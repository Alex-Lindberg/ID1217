/* matrix summation using pthreads

   features: uses a barrier; the Worker[0] computes
             the total sum from partial sums computed by Workers
             and prints the total sum to the standard output

   usage under Linux:
     gcc matrixSum.c -lpthread
     a.out size numWorkers

*/
#ifndef _REENTRANT 
#define _REENTRANT 
#endif 
#include <pthread.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <time.h>
#include <sys/time.h>
#define MAXSIZE 10000  /* maximum matrix size */
#define MAXWORKERS 10   /* maximum number of workers */

/* Assignment 1: finding Max and Min values + their index */
struct matrixInfo {
  long maxIdx, maxIdy;
  long minIdx, minIdy;
  int maxVal;
  int minVal;
  int total;
} worker;

pthread_mutex_t minValueLock; /* locks minimum value update */
pthread_mutex_t maxValueLock; /* locks maximum value update */
pthread_mutex_t bagLock;      /* locks row update */
pthread_mutex_t totalSumLock; /* locks total update */

int numWorkers;               /* number of workers */ 

/* timer */
double read_timer() {
    static bool initialized = false;
    static struct timeval start;
    struct timeval end;
    if( !initialized )
    {
        gettimeofday( &start, NULL );
        initialized = true;
    }
    gettimeofday( &end, NULL );
    return (end.tv_sec - start.tv_sec) + 1.0e-6 * (end.tv_usec - start.tv_usec);
}

double start_time, end_time; /* start and end times */
int size;  /* assume size is multiple of numWorkers */
int matrix[MAXSIZE][MAXSIZE]; /* matrix */

void *Worker(void *);

/* read command line, initialize, and create threads */
int main(int argc, char *argv[]) {
  int i, j;
  long l, k; /* use long in case of a 64-bit system */
  pthread_attr_t attr;
  pthread_t workerid[MAXWORKERS];

  /* set global thread attributes */
  pthread_attr_init(&attr);
  pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);

  /* Assignment 1: Mutex locks for Max/Min values */
  pthread_mutex_init(&minValueLock, NULL);
  pthread_mutex_init(&maxValueLock, NULL);
  pthread_mutex_init(&bagLock, NULL);
  pthread_mutex_init(&totalSumLock, NULL);

  /* read command line args if any */
  size = (argc > 1)? atoi(argv[1]) : MAXSIZE;
  numWorkers = (argc > 2)? atoi(argv[2]) : MAXWORKERS;
  if (size > MAXSIZE) size = MAXSIZE;
  if (numWorkers > MAXWORKERS) numWorkers = MAXWORKERS;

  /* initialize the matrix */
  time_t t;
  srand((unsigned) time(&t));
  
  for (i = 0; i < size; i++) {
	  for (j = 0; j < size; j++) {
          matrix[i][j] = rand()%99;
	  }
  }

  /* Assignment 1: Initialize worker struct */
  worker.maxIdx = 0;
  worker.maxIdy = 0;
  worker.minIdx = 0;
  worker.minIdy = 0;
  worker.maxVal = matrix[0][0];
  worker.minVal = matrix[0][0];
  worker.total = 0;

  /* print the matrix */
#ifdef DEBUG
  for (i = 0; i < size; i++) {
	  printf("[ ");
	  for (j = 0; j < size; j++) {
	    printf(" %d", matrix[i][j]);
	  }
	  printf(" ]\n");
  }
#endif

  /* do the parallel work: create the workers */
  start_time = read_timer();
  for (l = 0; l < numWorkers; l++)
    pthread_create(&workerid[l], &attr, Worker, (void *) l);

  for (k = 0; k < numWorkers; k++)
    pthread_join(workerid[k], NULL);

  /* get end time */
  end_time = read_timer();
  /* print results */
  printf("The total is %d\n", worker.total);
  printf("The maximum value is %d\n", worker.maxVal);
  printf("The minimum value is %d\n", worker.minVal);
  printf("The execution time is %g sec\n", end_time - start_time);
  
  pthread_exit(NULL);
}

int nextRow = 0;  /* Bag of tasks */

/* Each worker sums the values in one strip of the matrix.
   After a barrier, worker(0) computes and prints the total */
void *Worker(void *arg) {
  long myid = (long) arg;
  int i, j;
  int row, rowSum;
  int rowsComputed;

  #ifdef DEBUG
    printf("worker %d (pthread id %d) has started\n", myid, pthread_self());
  #endif
  
  rowsComputed = 0;
  /* sum values in my strip */
  while (true) {

    /* Get a task from the bag */
    pthread_mutex_lock(&bagLock);
    row = nextRow;
    nextRow++;
    pthread_mutex_unlock(&bagLock);
    if(row >= size)
      break;
    
    #ifdef DEBUG
      printf("worker %d takes row: %d\n", myid, row);
    #endif
    /* Computation */
    rowSum = 0;
    for (j = 0; j < size; j++){

      rowSum += matrix[row][j];
      
      if(matrix[row][j] > worker.maxVal) {
        /* prevent concurrent update */
        pthread_mutex_lock(&maxValueLock);
        /* check again in case maxVal changed */
        if(matrix[row][j] > worker.maxVal) {
          worker.maxIdx = row;
          worker.maxIdy = j;
          worker.maxVal = matrix[row][j];
        }
        pthread_mutex_unlock(&maxValueLock);
      }
      else if(matrix[row][j] < worker.minVal) {
        /* Same as for maxVal */
        pthread_mutex_lock(&minValueLock);
        if(matrix[row][j] < worker.minVal) {
          worker.minIdx = row;
          worker.minIdy = j;
          worker.minVal = matrix[row][j];
        }
        pthread_mutex_unlock(&minValueLock);
      }
    }
    pthread_mutex_lock(&totalSumLock);
    worker.total += rowSum;
    pthread_mutex_unlock(&totalSumLock);

    rowsComputed++;
  }
  printf("Worker %d computed %d rows\n", myid, rowsComputed);
}

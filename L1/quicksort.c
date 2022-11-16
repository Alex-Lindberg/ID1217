#ifndef _REENTRANT 
#define _REENTRANT 
#endif 
#include <pthread.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <time.h>
#include <sys/time.h>
#define MAXSIZE 10000000    /* maximum array size */
#define MAXWORKERS 10       /* maximum number of workers */
#define CUTOFFTHRESHOLD 32  /* switch to insertionsort when we reach small intervall sizes */

static void printNumbers(int *);
static int validateSorted();
static void *parallelQuicksort(void *);
static void swap(int, int);
void cutOffInsertionsort(int, int);
static int partition(int, int);
static int medianOfThree(int , int , int );
static double read_timer();

struct part {
    int lo;
    int hi;
};

int maxWorkers;                 /* number of usable workers */ 
int activeWorkers = 0;          /* number of currently active workers */
int size;                       /* size of the numbers array */
int numbers[MAXSIZE];           /* the numbers to sort */

pthread_mutex_t activeWorkersLock; /* locks minimum value update */

double start_time, end_time;

int main(int argc, char *argv[]) 
{    
    int i, j;
    pthread_attr_t attr;
    /* set global thread attributes */
    pthread_attr_init(&attr);
    pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);

    /* initialize mutex */
    pthread_mutex_init(&activeWorkersLock, NULL);

    /* read command line args if any */
    size = (argc > 1)? atoi(argv[1]) : MAXSIZE;
    maxWorkers = (argc > 2)? atoi(argv[2]) : MAXWORKERS;
    if (size > MAXSIZE) size = MAXSIZE;
    if (maxWorkers > MAXWORKERS) maxWorkers = MAXWORKERS;

    /* Initialize quicksort arguments*/
    struct part args = {0, size - 1};

    /* seed rand */
    time_t t;
    srand((unsigned) time(&t));

    /* initialize the numbers array */
    for (i = 0; i < size; i++) {
        numbers[i] = rand()%1000;
    }
    #ifdef DEBUG
        printf("Original:\n");
        printNumbers(numbers);
    #endif

    /* start timer */
    start_time = read_timer();

    /* Do parallel work */
    parallelQuicksort((void *)&args);

    /* get end time */
    end_time = read_timer();

    /* print results */
    #ifdef DEBUG
        printf("Result:\n");
        printNumbers(numbers);
    #endif
    printf("Is array sorted? : %s\n", (validateSorted()) ? "Yes" : "No");
    printf("The execution time is %g sec\n", end_time - start_time);
    pthread_exit(NULL);
}

/**
 * Helper function for quickSort().
 * 
 * @param array     The array to sort
 * @param lo        The starting point of the partition
 * @param hi        The end point of the partition
*/
static void *parallelQuicksort(void *arg)
{
    struct part *args = arg;
    
    int lo = args->lo;
    int hi = args->hi;

    /* if the start and end are equal or past each other the array is already sorted */
    if (hi <= lo) {
        pthread_mutex_lock(&activeWorkersLock);
        activeWorkers -= 1;
        pthread_mutex_unlock(&activeWorkersLock);
    }

    /* cutoff to insertionsort to increase efficiency for smaller intervalls */
    else if (hi - lo < CUTOFFTHRESHOLD) {
        pthread_mutex_lock(&activeWorkersLock);
        activeWorkers -= 1;
        pthread_mutex_unlock(&activeWorkersLock);
        cutOffInsertionsort(lo, hi + 1);
    }

    /* Quicksort */
    else {

        /* median of three pivot to eliminate worst case time complexity */
        int median = medianOfThree(lo, lo + (hi - lo) / 2, hi);
        swap(lo, median);

        int pivot = partition(lo, hi);
        struct part lo_args = {lo, pivot - 1};
        struct part hi_args = {pivot + 1, hi};

        pthread_mutex_lock(&activeWorkersLock);
        if(activeWorkers < maxWorkers) 
        {
            activeWorkers += 1;
            pthread_mutex_unlock(&activeWorkersLock);
            /* Create a new thread on the first partition */
            pthread_t worker;
            pthread_create(&worker, NULL, parallelQuicksort, (void *)&lo_args);

            /* Let current thread proceed with the other partition */
            parallelQuicksort((void *)&hi_args);

            /* Wait for the first partition to finish */
            if(pthread_join(worker, NULL))
            {
                printf("Thread %d failed to join\n");
                exit(-1);
            }
            else
            {
                pthread_mutex_lock(&activeWorkersLock);
                activeWorkers -= 1;
                pthread_mutex_unlock(&activeWorkersLock);
            }
        }
        else {
            pthread_mutex_unlock(&activeWorkersLock);
            /* Let current thread handle both partitions */
            parallelQuicksort((void *)&lo_args);
            parallelQuicksort((void *)&hi_args);
        }
    }
}

/**
 * Sorts elements in a partition in relation to the pivot index.
 * Swaps left and right indices based on a comparison with the pivot element until there 
 * are no elements larger than the pivot on the left hand side of the partition,
 * and not elements smaller than the pivot on the right hand side of the partition. 
 * 
 * @param array     The array to sort
 * @param pid       The index of the pivot element 
 * @param eid       The index of the end point of the partition
*/
static int partition(int pivot, int end)
{
    int i = pivot;
    int j = end + 1;
    #ifdef DEBUG
        printf("Sorting [%d, %d] on thread %d\n", i,j,pthread_self());
    #endif

    while (1)
    {
        // Find largest element from the left (comp. to pivot)
        while (numbers[++i] < numbers[pivot] && i != end){}
        // Find smallest element from the right (comp. to pivot)
        while (numbers[pivot] < numbers[--j] && j != pivot){}

        if (i >= j) break;  // exit if the indices pass each other
        swap(i, j);
    }
    swap(pivot, j);
    return j;
}

static void swap(int i, int j)
{
    int temp = numbers[i];
    numbers[i] = numbers[j];
    numbers[j] = temp;
}

/** 
 * Approximates the median and returns it. In quicksort this is used
 * to find a better pivot element for the algorithm.
 * 
 * @param a         An array
 * @param lo        The starting point of the partition
 * @param mid       The middle of the partition
 * @param hi        The end point of the partition
*/
static int medianOfThree(int lo, int mid, int hi)
{
    return ((numbers[lo] < numbers[mid]) ? 
        ((numbers[mid] < numbers[hi]) ? mid : (numbers[lo] < numbers[hi]) ? hi : lo) : 
        ((numbers[hi] < numbers[mid]) ? mid : (numbers[hi] < numbers[lo]) ? hi : lo));
}

void cutOffInsertionsort(int from, int to) 
{
    int i, j;
    for (i = from + 1; i < to; i++)
        for(j = i; j > 0 && numbers[j] < numbers[j - 1]; j--) 
            swap(j, j - 1);
} 

void printNumbers(int *numbers) {
    int i;
    printf("[");
    for (i = 0; i < size; i++)
    {
        printf("%d ", numbers[i]);
    } 
    printf("]\n");
}

int validateSorted() {
    int i, prev = numbers[0];
    for ( i = 0; i < size; i++)
    {
        if(numbers[i] < prev) {
            #ifdef DEBUG
                printf("\nid %d, %d !< %d\n",i,prev,numbers[i]);
            #endif
            return false;
        }
        prev = numbers[i];
    }
    return true;
}

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
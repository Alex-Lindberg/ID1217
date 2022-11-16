/* matrix summation using OpenMP

   usage with gcc (version 4.2 or higher required):
     gcc -O -fopenmp matrixSum.c -o matrixSum
     ./matrixSum-openmp size numWorkers

*/
#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#include <omp.h>

double start_time, end_time;

#include <stdio.h>
#define MAXSIZE 10000 /* maximum matrix size */
#define MAXWORKERS 8   /* maximum number of workers */

int numWorkers;
int size;
int matrix[MAXSIZE][MAXSIZE];

/* read command line, initialize, and create threads */
int main(int argc, char *argv[])
{
  int i, j, total = 0;

  // omp_lock_t minLock;
  // omp_init_lock(&minLock);

  // omp_lock_t maxLock;
  // omp_init_lock(&maxLock);

  /* read command line args if any */
  size = (argc > 1) ? atoi(argv[1]) : MAXSIZE;
  numWorkers = (argc > 2) ? atoi(argv[2]) : MAXWORKERS;
  if (size > MAXSIZE)
    size = MAXSIZE;
  if (numWorkers > MAXWORKERS)
    numWorkers = MAXWORKERS;

  omp_set_num_threads(numWorkers);

  time_t t;
  srand((unsigned)time(&t));

  /* initialize the matrix */
  for (i = 0; i < size; i++)
  {
    for (j = 0; j < size; j++)
    {
      matrix[i][j] = rand() % 1000;
    }
  }
  int minVal = matrix[0][0];
  int maxVal = matrix[0][0];
  int maxRow = 0, minRow = 0;
  int maxCol = 0, minCol = 0;

  start_time = omp_get_wtime();
#pragma omp parallel for reduction(+:total) private(j)
  for (i = 0; i < size; i++)
  {
    for (j = 0; j < size; j++)
    {
      total += matrix[i][j];

      if (matrix[i][j] < minVal)
      {
        // omp_set_lock(&minLock);
        #pragma omp critical(min)
        {
          if (matrix[i][j] < minVal)
          {
            minVal = matrix[i][j];
            minRow = i;
            minCol = j;
          }
        }
        // omp_unset_lock(&minLock);
      }
      if (matrix[i][j] > maxVal)
      {
        #pragma omp critical(max)
        {
          // omp_set_lock(&maxLock);
          if (matrix[i][j] > maxVal)
          {
            maxVal = matrix[i][j];
            maxRow = i;
            maxCol = j;
          }
        }// omp_unset_lock(&maxLock);
      }
    }
  }

  // implicit barrier

  end_time = omp_get_wtime();

  printf("the minimum value is %d at matrix[%d][%d]\n", minVal, minRow, minCol);
  printf("the maximum value is %d at matrix[%d][%d]\n", maxVal, maxRow, maxCol);
  printf("the total is %d\n", total);
  printf("it took %g seconds\n", end_time - start_time);
}

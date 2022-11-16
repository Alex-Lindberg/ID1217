
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <ctype.h>
#include <string.h>
#include <omp.h>

#define MAXSIZE 100000 /* maximum matrix size */
#define MAXWORDSIZE 64 /* maximum matrix size */
#define MAXWORKERS 16  /* maximum number of workers */

char wordMap[MAXSIZE][MAXWORDSIZE]; /* Map of every word from the file */
bool palindromicMap[MAXSIZE];        /* initialized to 0, 1 if palindromic */

FILE *words_f;
FILE *results_f;

bool binarySearch(int, char *);
void reverse(char *);

int main(int argc, char *argv[])
{
    double start_time, end_time;
    int numWorkers;
    int totalWords, i, k;
    int numWords = 0;

    /* CLI args, open files */
    numWorkers = (argc > 1) ? atoi(argv[1]) : 1;
    words_f = (argc > 2) ? fopen(argv[2], "r") : fopen("./words.txt", "r");
    results_f = (argc > 3) ? fopen(argv[3], "w") : fopen("./results.txt", "w");

    /* Thread count */
    omp_set_num_threads(numWorkers);

    /* Scan input word file */
    totalWords = 0;
    while (fscanf(words_f, "%s", wordMap[totalWords]) == 1)
    {
        for (i = 0; wordMap[totalWords][i]; i++)
        {
            wordMap[totalWords][i] = toupper(wordMap[totalWords][i]);
        }
        palindromicMap[totalWords] = false;
        totalWords++;
    }
    fclose(words_f);

    start_time = omp_get_wtime();

    /* Start of parallel work */
#pragma omp parallel for reduction(+ \
                                   : numWords) private(k)
    for (k = 0; k < totalWords; k++)
    {
        char word[MAXWORDSIZE];
        strcpy(word, wordMap[k]);
        reverse(word);
        /* Since the "words" file is sorted we can use binary search
            to find if our reversed word is in the wordMap */
        if (strcmp(word, wordMap[k]) == 0 || binarySearch(totalWords, word))
        {
            palindromicMap[k] = true;
            numWords += 1;
        }
    }
    /* End of parallel work */

    end_time = omp_get_wtime();

    /* Write results to file */
    for (i = 0; i < totalWords; i++)
    {
        if (palindromicMap[i])
            fprintf(results_f, "%s\n", wordMap[i]);
    }
    fclose(results_f);
#ifdef RESULT
    printf("%d\t&%g \\\\", numWorkers, end_time - start_time);
#else
    printf("Executed in %g seconds\n", end_time - start_time);
    printf("Number of palindromic words: %d\n", numWords);
#endif
}

/* //////////////////////// */
/* ------------------------ */

/* Performs binary search over wordMap */
bool binarySearch(int end, char *word)
{
    int match, mid, start = 0;
    while (start <= end)
    {
        mid = (start + end) / 2;
        match = strcmp(word, wordMap[mid]);
        if (match == 0)
            return true;
        if (match < 0)
            end = mid - 1;
        else if (match > 0)
            start = mid + 1;
    }
    return false;
}

/* Reverses a string */
void reverse(char *word)
{
    int i = 0;
    int len = strlen(word);
    int temp;

    for (i = 0; i < len / 2; i++)
    {
        temp = word[i];
        word[i] = word[len - i - 1];
        word[len - i - 1] = temp;
    }
}
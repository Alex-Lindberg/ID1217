#ifndef _REENTRANT 
#define _REENTRANT 
#endif 
#include <pthread.h>
#include <stdlib.h>
#include <stdbool.h>
#include <stdio.h>

#define MAXCHARS 256

void *readStdin();
void *writeStdout();
void *writeFile();

pthread_mutex_t bufferLock;
pthread_mutex_t readLock;
pthread_mutex_t barrier;
pthread_cond_t bufferGo;
pthread_cond_t readGo;
int numArrived = 0;

char *buffer;
size_t size = 0;
ssize_t read = -1;
FILE *file;
bool done = false;

void Barrier() {
    pthread_mutex_lock(&barrier);
    numArrived++;
    if(numArrived == 3) {
        read = -1;
        numArrived = 0;
        pthread_mutex_unlock(&readLock);
        pthread_cond_broadcast(&readGo);
    } else
        pthread_cond_wait(&readGo, &barrier);
    pthread_mutex_unlock(&barrier);
}

int main(int argc, char *argv[])
{
    pthread_attr_t attr;
    /* set global thread attributes */
    pthread_attr_init(&attr);
    pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);

    /* Initialize mutexs and conditions */
    pthread_mutex_init(&bufferLock, NULL);
    pthread_mutex_init(&readLock, NULL);
    pthread_mutex_init(&barrier, NULL);
    pthread_cond_init(&bufferGo, NULL);
    pthread_cond_init(&readGo, NULL);

    file = fopen(argv[1], "w");

    pthread_t readIn, writeOut, writefile;

    /* Parallel work */
    pthread_create(&readIn, &attr, readStdin, NULL);
    pthread_create(&writeOut, &attr, writeStdout, NULL);
    pthread_create(&writefile, &attr, writeFile, NULL);

    /* Join threads */
    pthread_join(readIn, NULL);
    pthread_join(writeOut, NULL);
    pthread_join(writefile, NULL);

    fclose(file);
}

void *readStdin() 
{
    while (true)
    {
        /* Lock the buffer */
        pthread_mutex_lock(&bufferLock);

        /* Read a line from stdin */
        if((read = getline(&buffer,&size, stdin)) > 0) 
        {
            /* Unlock the buffer */
            pthread_mutex_unlock(&bufferLock);

            /* Lock the reader so the writers can finish */
            pthread_mutex_lock(&readLock);

            Barrier();
        }
        else {
            done = true;
            break;
        }
    }
    free(buffer);
    pthread_exit(0);
}

void *writeStdout() 
{
    while(!done)
    {
        pthread_mutex_lock(&bufferLock);
        /* Check if reader has filled the buffer */
        if(read > 0) 
        {
            pthread_mutex_unlock(&bufferLock);
            printf("%s",buffer);

            Barrier();
        }
        else
            pthread_mutex_unlock(&bufferLock);
    }
    pthread_exit(0);
}

void *writeFile() 
{
    while(!done)
    {
        pthread_mutex_lock(&bufferLock);
        /* Check if reader has filled the buffer */
        if(read > 0) 
        {
            pthread_mutex_unlock(&bufferLock);
            fputs(buffer, file); 
            fflush(file);

            Barrier();
        }
        else
            pthread_mutex_unlock(&bufferLock);
    }
    pthread_exit(0);
}
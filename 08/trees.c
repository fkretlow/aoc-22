#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

#define HEIGHT 99
#define WIDTH 99
#define INPUT_FILE_NAME "input.txt"

char field[HEIGHT][WIDTH];

void read_input_to_field(FILE* f) {
    for (int i = 0; i < HEIGHT; ++i) {
        for (int j = 0; j < WIDTH; ++j) {
            field[i][j] = fgetc(f) - '0';
        }
        fgetc(f); // ignore the newline char
    }
}

size_t count_visible_trees() {
    bool visibility[HEIGHT][WIDTH] = { false };

    for (int i = 1; i < HEIGHT - 1; ++i) {
        char max = field[i][0];
        for (int j = 1; j < WIDTH - 1; ++j) {
            if (field[i][j] > max) {
                max = field[i][j];
                visibility[i][j] = true;
            }
        }
        max = field[i][WIDTH-1];
        for (int j = WIDTH-2; j > 0; --j) {
            if (field[i][j] > max) {
                max = field[i][j];
                visibility[i][j] = true;
            }
        }
    }

    for (int j = 1; j < WIDTH - 1; ++j) {
        char max = field[0][j];
        for (int i = 1; i < HEIGHT - 1; ++i) {
            if (field[i][j] > max) {
                max = field[i][j];
                visibility[i][j] = true;
            }
        }
        max = field[HEIGHT-1][j];
        for (int i = HEIGHT-2; i > 0; --i) {
            if (field[i][j] > max) {
                max = field[i][j];
                visibility[i][j] = true;
            }
        }
    }

    size_t count = 2 * (HEIGHT + WIDTH) - 4;
    for (int i = 1; i < HEIGHT - 1; ++i) {
        for (int j = 1; j < WIDTH - 1; ++j) {
            if (visibility[i][j]) ++count;
        }
    }

    return count;
}

int main(int argc, char* argv[]) {
    FILE* f =  fopen(INPUT_FILE_NAME, "r");
    if (!f) {
        printf("Error: Cannot open input file.\n");
        return EXIT_FAILURE;
    }
    read_input_to_field(f);
    fclose(f);

    printf("There are %zu visible trees.\n", count_visible_trees());
    return EXIT_SUCCESS;
}

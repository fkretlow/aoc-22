#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

#define HEIGHT 9
#define WIDTH 9
#define INPUT_FILE_NAME "input.txt"

char field[HEIGHT][WIDTH];

void read_input(FILE* f) {
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

unsigned scenic_score(int i0, int j0) {
    printf("%d:%d=%d\n", i0, j0, field[i0][j0]);
    unsigned up = 0;
    printf("up:\n");
    for (int i = i0-1; i >= 0; --i) {
        printf("  %d:%d=%d\n", i, j0, field[i][j0]);
        ++up;
        if (field[i][j0] >= field[i0][j0] || field[i][j0] == field[i+1][j0]) break;
    }
    printf("up: %d\n", up);

    unsigned left = 0;
    printf("left:\n");
    for (int j = j0-1; j >= 0; --j) {
        printf("  %d:%d=%d\n", i0, j, field[i0][j]);
        ++left;
        if (field[i0][j] >= field[i0][j0] || field[i0][j] == field[i0][j+1]) break;
    }
    printf("left: %d\n", left);

    unsigned down = 0;
    printf("down:\n");
    for (int i = i0+1; i < HEIGHT; ++i) {
        printf("  %d:%d=%d\n", i, j0, field[i][j0]);
        ++down;
        if (field[i][j0] >= field[i0][j0] || field[i][j0] == field[i-1][j0]) break;
    }
    printf("down: %d\n", down);

    unsigned right = 0;
    printf("right:\n");
    for (int j = j0+1; j < WIDTH; ++j) {
        printf("  %d:%d=%d\n", i0, j, field[i0][j]);
        ++right;
        if (field[i0][j] >= field[i0][j0] || field[i0][j] == field[i0][j-1]) break;
    }
    printf("right: %d\n", right);

    return up * left * down * right;
}

unsigned best_scenic_score() {
    unsigned max = 0;
    for (int i = 0; i < HEIGHT; ++i) {
        for (int j = 0; j < WIDTH; ++j) {
            unsigned score = scenic_score(i, j);
            if (score > max) max = score;
        }
    }
    return max;
}

int main(int argc, char* argv[]) {
    FILE* f =  fopen(INPUT_FILE_NAME, "r");
    if (!f) {
        printf("Error: Cannot open input file.\n");
        return EXIT_FAILURE;
    }
    read_input(f);
    fclose(f);

    printf("There are %zu visible trees.\n", count_visible_trees());
    printf("The best scenic score is %u.\n", best_scenic_score());
    return EXIT_SUCCESS;
}

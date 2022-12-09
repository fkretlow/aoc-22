def move_knot(k, m):
    return tuple(x+dx for x,dx in zip(k, m))

def do_knots_touch(k1, k2):
    return all(abs(a - b) <= 1 for a,b in zip(k1, k2))

def calculate_follow_move(k1, k2):
    return tuple(signed_one(a-b) for a,b in zip(k1, k2))

def signed_one(x):
    if   x < 0: return -1
    elif x > 0: return  1
    else:       return  0

def parse_moves(line):
    if   line[0] == "U": move = ( 0, 1)
    elif line[0] == "L": move = (-1, 0)
    elif line[0] == "D": move = ( 0,-1)
    elif line[0] == "R": move = ( 1, 0)
    else: raise ValueError(f"invalid move direction '{line}'")
    return (move for _ in range(int(line[2:])))

def calculate_visited_fields(lines, num_knots = 1):
    knots = [(0,0) for _ in range(num_knots)]
    visited_fields = set() # (0,0) will be added no matter what after the first move
    for line in lines:
        for move in parse_moves(line):
            knots[0] = move_knot(knots[0], move)
            for i in range(1, len(knots)):
                prev = knots[i-1]
                cur = knots[i]
                if do_knots_touch(prev, cur): break
                follow_move = calculate_follow_move(prev, cur)
                knots[i] = move_knot(knots[i], follow_move)
            visited_fields.add(knots[-1])
    return len(visited_fields)


if __name__ == "__main__":
    with open("input.txt", "r") as f:
        r = calculate_visited_fields(f, 10)
        print(f"{r} fields were visited.")

import re


class Interval(object):
    pattern = re.compile("(?P<lo>\d+)-(?P<hi>\d+)")

    def __init__(self, lo, hi):
        self.lo = lo
        self.hi = hi

    def fully_contains(self, other):
        return self.lo <= other.lo and self.hi >= other.hi

    def overlaps(self, other):
        return (self.lo <= other.hi and self.hi >= other.lo) \
            or (other.lo <= self.hi and other.hi >= self.lo)

    def __repr__(self):
        return f"{self.lo}..{self.hi}"

    @classmethod
    def from_string(cls, s):
        match = cls.pattern.match(s)
        if not match: raise ValueError(f"cannot parse '{s}' as {cls.__name__}")
        return cls(*(int(g) for g in match.groups()))


def parse_intervals(line):
    return (Interval.from_string(word) for word in line.split(","))

def has_containment(line):
    fst, snd = parse_intervals(line)
    return fst.fully_contains(snd) or snd.fully_contains(fst)

def has_overlap(line):
    fst, snd = parse_intervals(line)
    return fst.overlaps(snd)


if __name__ == "__main__":
    with open("input.txt", "r") as f:
        lines = f.readlines()
    print(sum(1 for line in lines if has_containment(line)))       
    print(sum(1 for line in lines if has_overlap(line)))

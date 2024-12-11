package model;

import java.util.Comparator;

public class PerformanceComparatorSize implements Comparator<Performance>
{
    @Override
    public int compare(Performance o1, Performance o2) {
        return o1.getSize() - o2.getSize();
    }
}

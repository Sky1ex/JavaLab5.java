package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Objects;

@AllArgsConstructor
public class PerformanceWithDates
{
    @Getter
    String name;
    @Getter
    String ageLimit;
    /*@Getter
    String size;*/
    @Getter
    int size;
    @Getter
    String imgAddress;
    @Getter
    ArrayList<String> dates = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        Performance performance = (Performance) o;
        return Objects.equals(name, performance.name);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

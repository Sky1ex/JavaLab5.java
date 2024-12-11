package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
public class Performance
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
    String date;
    @Getter
    String imgAddress;

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

package SubClasses;

import lombok.Getter;
import model.Performance;

@Getter
public class Miniperformance
{
    private String name;
    private int size;
    private String date;
    private String imgAddress;

    // Конструктор, который принимает объект Performance
    public Miniperformance(Performance performance) {
        this.name = performance.getName();
        this.size = performance.getSize();
        this.date = performance.getDate();
        this.imgAddress = performance.getImgAddress();
    }
}

package SubClasses;

import model.Performance;

import javax.swing.text.Document;
import java.util.ArrayList;

public class PerfAndDate
{
    private Performance performance;
    private ArrayList<String> date;

    public PerfAndDate(Performance performance, String link)
    {
        this.performance = performance;
    }
}

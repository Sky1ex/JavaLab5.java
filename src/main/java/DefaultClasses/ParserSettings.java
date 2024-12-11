package DefaultClasses;

import lombok.Getter;

@Getter
public abstract class ParserSettings
{
    public static String BASE_URL;

    public static String PREFIX;

    protected int startPoint;

    protected int endPoint;
}
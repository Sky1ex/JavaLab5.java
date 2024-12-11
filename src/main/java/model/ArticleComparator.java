package model;

import java.util.Comparator;

public class ArticleComparator implements Comparator<Article>
{
    @Override
    public int compare(Article o1, Article o2) {
        return o1.getName().compareTo(o2.getName());
    }
}

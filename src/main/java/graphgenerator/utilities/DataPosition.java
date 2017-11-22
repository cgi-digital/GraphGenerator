package graphgenerator.utilities;

public class DataPosition implements Comparable<DataPosition>{
    public String name;
    public int start;
    public int finish;

    public DataPosition(String name, int start) {
        this.name = name;
        this.start = start;
    }

    public void setFinish(int finish) {
        this.finish = finish;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Start: " + start + ", Finish: " + finish;
    }

    @Override
    public int compareTo(DataPosition o2) {
        if(o2.start < this.start)
            return 1;
        if(o2.start > this.start)
            return -1;
        return 0;
    }

}
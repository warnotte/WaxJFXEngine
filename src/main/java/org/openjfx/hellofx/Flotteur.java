package org.openjfx.hellofx;

public class Flotteur {
    private String name;
    private double x;
    private double y;
    private double rotation;

    public Flotteur(String name, double x, double y, double rotation) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRotation() {
        return rotation;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    @Override
    public String toString() {
        return "Flotteur{" +
               "name='" + name + '\'' +
               ", x=" + x +
               ", y=" + y +
               ", rotation=" + rotation +
               '}';
    }
}

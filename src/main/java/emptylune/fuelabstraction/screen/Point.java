package emptylune.fuelabstraction.screen;

public record Point(int x, int y) {
    public Point offset(Point point) {
        return offset(point.x, point.y);
    }

    public Point offset(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }
}

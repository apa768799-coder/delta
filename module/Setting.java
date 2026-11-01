package com.parallax.delta.module;

import java.util.List;
import java.util.function.Supplier;

public abstract class Setting<T> {
    protected final String name;
    protected T value;
    protected Supplier<Boolean> visible = () -> true;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public T getValue() { return value; }
    public void setValue(T v) { this.value = v; }
    public Setting<T> visibleWhen(Supplier<Boolean> v) { this.visible = v; return this; }
    public boolean isVisible() { return visible.get(); }

    public static class Num extends Setting<Double> {
        private final double min, max;
        public Num(String name, double def, double min, double max) {
            super(name, def);
            this.min = min;
            this.max = max;
        }
        @Override public void setValue(Double v) {
            super.setValue(Math.max(min, Math.min(max, v)));
        }
        public double get() { return value; }
        public int getInt() { return (int) Math.round(value); }
        public float getFloat() { return value.floatValue(); }
        public double getMin() { return min; }
        public double getMax() { return max; }
    }

    public static class Bool extends Setting<Boolean> {
        public Bool(String name, boolean def) { super(name, def); }
        public boolean get() { return value; }
    }

    public static class Mode extends Setting<String> {
        private final List<String> modes;
        public Mode(String name, String def, String... modes) {
            super(name, def);
            this.modes = List.of(modes);
        }
        public List<String> getModes() { return modes; }
        public boolean is(String m) { return value.equals(m); }
        public void cycle() {
            int i = modes.indexOf(value);
            super.setValue(modes.get((i + 1) % modes.size()));
        }
        @Override public void setValue(String v) {
            if (modes.contains(v)) super.setValue(v);
        }
    }
}

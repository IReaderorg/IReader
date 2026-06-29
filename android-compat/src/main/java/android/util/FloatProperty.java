package android.util;
public abstract class FloatProperty<T> {
    public FloatProperty(String name) {}
    public abstract float get(T object);
    public abstract void set(T object, float value);
}

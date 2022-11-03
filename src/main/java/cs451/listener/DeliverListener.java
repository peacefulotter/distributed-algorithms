package cs451.listener;

public interface ChangeListener<T>
{
    void actionPerformed( T value );
}
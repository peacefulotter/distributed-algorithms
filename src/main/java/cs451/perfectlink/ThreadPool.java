package cs451.perfectlink;

import cs451.packet.Packet;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPool
{
    private final Sender sender;
    private final ExecutorService service;
    private Map<Integer, PacketTask> queue;

    public ThreadPool( Sender sender, int nThreads )
    {
        this.sender = sender;
        this.service = Executors.newFixedThreadPool( nThreads );
        this.queue = new HashMap<>();
    }

    public void submitTask( int seqNr, int messages )
    {
        PacketTask task = new PacketTask( this, sender, seqNr, messages );
        // sequential broadcast and register
        sender.log("POOL", "Sequentially sending " + seqNr + " " + messages);
        if ( task.broadcast() )
            task.register();
        // add task to queue map
        queue.put( seqNr, task );
        // submit task to thread pool
        service.submit(task);
    }

    public void onAckReceive( Packet ack )
    {
        PacketTask task = queue.get( ack.getSeqNr() );
        sender.log("POOL", "onAckReceive - " + ack + " task: " + task );
        if ( task == null ) return;
        task.onAck( ack );
    }

    public void removeTask( int seqNr )
    {
        queue.remove( seqNr );
    }

    public boolean awaitTermination()
    {
        service.shutdown();
        try
        {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch ( InterruptedException e )
        {
            throw new RuntimeException( e );
        }
        return false;
    }

    public void shutdown()
    {
        sender.log( "POOL", "Shutdown" );
        service.shutdownNow();
    }
}

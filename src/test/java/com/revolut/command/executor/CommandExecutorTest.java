package com.revolut.command.executor;

import com.revolut.command.Command;
import com.revolut.lock.LockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandExecutorTest {

    @Test
    public void testSuccessfulPath(@Mock Command command, @Mock LockService lockService) throws Exception {
        CommandExecutor commandExecutor = new CommandExecutor(lockService);
        commandExecutor.execute(command);

        verify(command, times(1)).pre(lockService);
        verify(command, times(1)).execute();
        verify(command, times(1)).onSuccess();
        verify(command, times(1)).release();
        verify(command, never()).onFailure(any());
    }

    @Test
    public void testExceptionInPreIsRethrownAndIsFailureIsCalled(@Mock Command command, @Mock LockService lockService) throws Exception {
        doThrow(RuntimeException.class).when(command).pre(any());

        CommandExecutor commandExecutor = new CommandExecutor(lockService);

        assertThrows(RuntimeException.class, () -> commandExecutor.execute(command));

        verify(command, times(1)).pre(lockService);
        verify(command, never()).execute();
        verify(command, never()).onSuccess();
        verify(command, times(1)).release();
        verify(command, times(1)).onFailure(any());
    }

    @Test
    public void testExceptionInExecuteIsRethrownAndIsFailureIsCalled(@Mock Command command, @Mock LockService lockService) throws Exception {
        doThrow(RuntimeException.class).when(command).execute();

        CommandExecutor commandExecutor = new CommandExecutor(lockService);

        assertThrows(RuntimeException.class, () -> commandExecutor.execute(command));

        verify(command, times(1)).pre(lockService);
        verify(command, times(1)).execute();
        verify(command, never()).onSuccess();
        verify(command, times(1)).release();
        verify(command, times(1)).onFailure(any());
    }

    @Test
    public void testExceptionInOnSuccessIsRethrownAndIsFailureIsCalled(@Mock Command command, @Mock LockService lockService) throws Exception {
        doThrow(RuntimeException.class).when(command).onSuccess();

        CommandExecutor commandExecutor = new CommandExecutor(lockService);

        assertThrows(RuntimeException.class, () -> commandExecutor.execute(command));

        verify(command, times(1)).pre(lockService);
        verify(command, times(1)).execute();
        verify(command, times(1)).onSuccess();
        verify(command, times(1)).release();
        verify(command, times(1)).onFailure(any());
    }
}

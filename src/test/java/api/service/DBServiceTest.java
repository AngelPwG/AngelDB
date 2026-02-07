package api.service;

import btree.BPlusTree;
import models.Row;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.*;

import static org.mockito.Mockito.*;

public class DBServiceTest {

    @Mock
    private BPlusTree engine;

    @InjectMocks
    private DBService service;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void insertRecord_shouldSucceed_whenInputIsValid() {
        when(service.engine.insert(anyLong(), any(Row.class))).thenReturn(true);

        service.insertRecord(100, "Batman", 35);

        verify(service.engine).insert(eq(100L), any(Row.class));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void insertRecord_shouldThrowException_whenIdExists() {
        when(service.engine.insert(anyLong(), any(Row.class))).thenReturn(false);

        service.insertRecord(100, "Batman", 35);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Age cannot be negative or above 110 years.")
    public void insertRecord_shouldThrowException_whenAgeIsInvalid() {
        service.insertRecord(100, "Batman", -5);
    }

    @Test
    public void select_shouldSuccess_whenRecordIsFound() {
        when(service.engine.search(anyLong())).thenReturn(mock(Row.class));

        service.selectRecord(30);
        verify(service.engine).search(30);
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "Record with ID 30 not found.")
    public void select_shouldThrowException_whenRecordIsNotFound() {
        when(service.engine.search(anyLong())).thenReturn(null);

        service.selectRecord(30);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Start ID cannot be greater than End ID.")
    public void selectRange_shouldThrowException_whenStartIsGreaterThanEnd() {
        service.selectRange(30, 10);
    }
}
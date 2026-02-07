package api.service;

import btree.BPlusTree;
import models.Row;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class DBServiceTest {

    @Mock
    private BPlusTree engine;

    private DBService service;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new DBService(engine);
    }


    @Test
    public void insertRecord_shouldSucceed_whenInputIsValid() {
        when(engine.insert(anyLong(), any(Row.class))).thenReturn(true);

        service.insertRecord(100, "Batman", 35);

        verify(engine).insert(eq(100L), any(Row.class));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void insertRecord_shouldThrowException_whenIdExists() {
        when(engine.insert(anyLong(), any(Row.class))).thenReturn(false);

        service.insertRecord(100, "Batman", 35);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Age cannot be negative or above 110 years.")
    public void insertRecord_shouldThrowException_whenAgeIsInvalid() {
        service.insertRecord(100, "Batman", -5);
    }

    @Test
    public void select_shouldSuccess_whenRecordIsFound() {
        when(engine.search(anyLong())).thenReturn(mock(Row.class));

        service.selectRecord(30);
        verify(engine).search(30);
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "Record with ID 30 not found.")
    public void select_shouldThrowException_whenRecordIsNotFound() {
        when(engine.search(anyLong())).thenReturn(null);

        service.selectRecord(30);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Start ID cannot be greater than End ID.")
    public void selectRange_shouldThrowException_whenStartIsGreaterThanEnd() {
        service.selectRange(30, 10);
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "There are no records between ID 10 and 20.")
    public void selectRange_shouldThrowException_whenNoRecordsFound() {
        when(engine.selectBetween(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        service.selectRange(10, 20);
    }

    @Test
    public void selectRange_shouldSuccess_whenRecordsFound() {
        List<Row> fakeRows = List.of(new Row(10L, "A", 20));
        when(engine.selectBetween(anyLong(), anyLong())).thenReturn(fakeRows);

        service.selectRange(10, 20);
        verify(engine).selectBetween(10, 20);
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "There are no records.")
    public void selectAll_shouldThrowException_whenNoRecordsFound() {
        when(engine.selectAll()).thenReturn(new ArrayList<>());

        service.selectAll();
    }

    @Test
    public void selectAll_shouldSuccess_whenRecordsFound() {
        List<Row> fakeRows = List.of(new Row(10L, "A", 20));
        when(engine.selectAll()).thenReturn(fakeRows);

        service.selectAll();
        verify(engine).selectAll();
    }
}
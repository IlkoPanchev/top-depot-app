package warehouse.utils.time;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public interface TimeBordersConvertor {

    LocalDateTime[] getTimeBordersAsLocalDateTime(String fromDate, String toDate, LocalDateTime firstOrderDateTime);

    String[] getTimeBordersAsString(String fromDate, String toDate, LocalDateTime firstOrderDateTime);

    HashMap<Integer, String> getBordersAndNamesMap(HashMap<Integer, String> namesMap, String[] timeBorders, List<Object[]> result);
}

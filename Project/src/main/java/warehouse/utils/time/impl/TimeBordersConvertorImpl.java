package warehouse.utils.time.impl;

import warehouse.utils.time.TimeBordersConvertor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public class TimeBordersConvertorImpl implements TimeBordersConvertor {
    @Override
    public LocalDateTime[] getTimeBordersAsLocalDateTime(String fromDate, String toDate, LocalDateTime firstOrderDateTime) {

        LocalDateTime localDateTimeFrom;
        LocalDateTime localDateTimeTo;
        DateTimeFormatter dateTimeFormatter ;
        LocalDate localDateFrom;
        LocalDate localDateTo;
        LocalDateTime[] timeBorders = new LocalDateTime[2];

        if (fromDate.equals("") && toDate.equals("")){
            localDateTimeFrom = firstOrderDateTime;
            localDateTimeTo = LocalDateTime.now();
        }
        else {
            dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            localDateFrom = LocalDate.parse(fromDate, dateTimeFormatter);
            localDateTimeFrom = localDateFrom.atStartOfDay();
            localDateTo = LocalDate.parse(toDate, dateTimeFormatter);
            localDateTimeTo = localDateTo.atStartOfDay().plusDays(1);

        }
        timeBorders[0] = localDateTimeFrom;
        timeBorders[1] = localDateTimeTo;

        return timeBorders;
    }

    @Override
    public String[] getTimeBordersAsString(String fromDate, String toDate, LocalDateTime firstOrderDateTime) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        ;
        String[] timeBorders = new String[2];
        LocalDateTime localDateTimeFrom;
        LocalDateTime localDateTimeTo;


        if (fromDate.equals("") && toDate.equals("")) {
            localDateTimeFrom = firstOrderDateTime;
            localDateTimeTo = LocalDateTime.now();
            fromDate = dateTimeFormatter.format(localDateTimeFrom);
            toDate = dateTimeFormatter.format(localDateTimeTo);
        }

        timeBorders[0] = fromDate;
        timeBorders[1] = toDate;

        return timeBorders;
    }

    @Override
    public HashMap<Integer, String> getBordersAndNamesMap(HashMap<Integer, String> namesMap,
                                                          String[] timeBorders,
                                                          List<Object[]> result) {
        namesMap.put(1, timeBorders[0]);
        namesMap.put(2, timeBorders[1]);

        int key = 2;
        for (Object[] objects : result) {
            namesMap.put(++key, String.valueOf(objects[0]));
        }

        return namesMap;
    }
}

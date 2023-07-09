package org.bgerp.plugin.clb.calendar.model.event.generator;
// package org.bgerp.plugin.clb.calendar.model.event;

// import java.time.LocalDate;
// import java.time.Year;
// import java.util.ArrayList;
// import java.util.List;

// import ru.bgcrm.util.Config;
// import ru.bgcrm.util.ParameterMap;

// /**
//  * Processor of event list.
//  *
//  * @author Shamil Vakhitov
//  */
// public abstract class EventProcessor extends Config {
//     protected EventProcessor(ParameterMap config) {
//         super(null);
//     }

//     /**
//      * Generates an event per day of a year.
//      * @param year
//      * @return
//      */
//     public List<Event> getEvents(int year) {
//         final int days = Year.of(year).length();
//         final int minuteTo = 24 * 60;

//         var result = new ArrayList<Event>(days);

//         for (int d = 1; d <= days; d++) {
//             var date = LocalDate.ofYearDay(year, d);
//             var event = new Event()
//                 .withDate(date)
//                 .withMinuteFrom(0)
//                 .withMinuteTo(minuteTo);
//             result.add(event);
//         }

//         return result;
//     }

//     public abstract List<Event> process(List<Event> events);
// }

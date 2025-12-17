import 'package:budgiet/main.dart';
import 'package:flutter/material.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:intl/intl.dart';

class NewTransactionFormState extends State<NewTransactionForm> {
  @override
  void initState() {
    super.initState();
    initializeDateFormatting().then((_) => setState(() {}));
  }

  DateTime selectedDate = () { // No scope :(
    DateTime now = DateTime.now().toLocal();
    return DateTime(now.year, now.month, now.day);
  }();

  Future<void> _selectDate() async {
    final DateTime? date = await showDatePicker(
      context: context,
      initialDate: selectedDate,
      firstDate: DateTime(0),
      lastDate: selectedDate,
    );
    if (date != null) {
      setState(() { selectedDate = date; });
    }
  }
  String formatDate() {
    String locale = Localizations.localeOf(context).languageCode;
    DateTime now = DateTime.now();

    if (selectedDate.day == now.day) {
      return "Today";
    } else if (selectedDate.day == now.day - 1) {
      return "Yesterday";
    } else {
      String dayOfWeek = DateFormat.EEEE(locale).format(selectedDate);
      String month = DateFormat.LLL(locale).format(selectedDate);
      String dayOfMonth = DateFormat.d(locale).format(selectedDate);
      String year;
      if (selectedDate.year != now.year) {
        year = ", ${DateFormat.y(locale).format(selectedDate)}";
      } else {
        year = "";
      }
      return "$dayOfWeek, $month $dayOfMonth$year";
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListView(
      controller: super.widget.controller,
      scrollDirection: .vertical,
      children: [
        _FormField(
          name: "Date",
          children: [ OutlinedButton(
            onPressed: _selectDate,
            child: Text(formatDate()),
          ) ],
        ),
        _FormField(
          name: "Location",
          children: [
            OutlinedButton(
              onPressed: () { /* TODO: Show modal to search location from user-defined list */ },
              child: Text("Location Name @ Location Address"),
            ),
            IconButton.outlined(
              onPressed: () { /* TODO: Get a location with this GPS coordinates, or prompt user to create new location */ },
              icon: Icon(Icons.pin_drop_outlined),
            ),
          ]
        ),
        _FormField(
          name: "Tags",
          children: [
            // ListView(
            //   scrollDirection: .horizontal,
            //   children: [
            //     // TODO: show most used or recently used tags
            //     Chip(
            //       surfaceTintColor: Colors.limeAccent,
            //       avatar: Icon(Icons.catching_pokemon),
            //       label: Text("My Tag"),
            //     ),
            //   ],
            // ),
            IconButton.filled(
              onPressed: () { /* TODO: show small modal with search bar and horizontal+wrapped list of tags */ },
              icon: Icon(Icons.search, color: ThemeData().colorScheme.onPrimary)
            )
          ],
        ),
        _FormField(
          name: "Amount",
          children: [
            // TextField(
            //   decoration: InputDecoration(
            //     icon: Icon(Icons.money), // TODO: use symbol of currency (e.g. $, DOM$, etc.),
            //     helperText: "0.00",
            //   ),
            //   textInputAction: .next,
            // )
          ],
        ),
      ],
    );
  }
}

class _FormField extends StatelessWidget {
  const _FormField({required this.name, required this.children});
  final String name;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      title: Row(children: [
        Text(name, softWrap: false),
        
        Expanded(child: Align(
          alignment: .centerRight,
          child: Row(
            mainAxisAlignment: .end,
            mainAxisSize: .min,
            children: children,
          ),
        )),
      ]),
    );
  }
}

// class Location {
//   String name;
//   String address;
//   String coords;
// }

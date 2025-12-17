import 'package:budgiet/bottom_sheet.dart';
import 'package:budgiet/new_transaction.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widget_previews.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: .fromSeed(seedColor: Colors.deepPurple),
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  // @Preview(name: 'My Sample Text')
  const MyHomePage({super.key, required this.title});
  
  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: .center,
          children: [
            Text("Press the '+ Transaction' button to get started.")
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        icon: Icon(Icons.add),
        label: Text("Transaction"),
        tooltip: "Add new transaction record",
        onPressed: () { showDraggableScrollableModalBottomSheet(context: context, builder: (BuildContext context, ScrollController controller) {
          return NewTransactionForm(controller: controller);
        },); },
      ),
    );
  }
}

class NewTransactionForm extends StatefulWidget {
  @Preview(name: "New Transaction")
  const NewTransactionForm({super.key, this.controller});

  final ScrollController? controller;

  @override
  State<NewTransactionForm> createState() => NewTransactionFormState();
}

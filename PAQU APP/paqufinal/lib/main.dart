import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:paqufinal/config/theme.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:paqufinal/screens/welcome_screen.dart';
import 'package:paqufinal/screens/goal_selection_screen.dart';
import 'package:paqufinal/screens/home_screen.dart';
import 'firebase_options.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  try {
    await Firebase.initializeApp(
      options: DefaultFirebaseOptions.currentPlatform,
    );
    debugPrint('✅ Firebase inicializado correctamente');
  } catch (e) {
    debugPrint('❌ Error inicializando Firebase: $e');
  }
  
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ScreenUtilInit(
      designSize: const Size(375, 812),
      minTextAdapt: true,
      splitScreenMode: true,
      builder: (context, child) {
        return MaterialApp(
          title: 'Paqu - Aprende Quechua',
          debugShowCheckedModeBanner: false,
          theme: AppTheme.lightTheme,
          home: const WelcomeScreen(), // Solo un home
          routes: {
            '/home': (context) => const HomeScreen(),
            '/goal-selection': (context) => const GoalSelectionScreen(isFirstTime: false),
          },
        );
      },
    );
  }
}
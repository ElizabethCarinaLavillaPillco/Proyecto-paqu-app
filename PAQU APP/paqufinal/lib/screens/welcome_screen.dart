import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:lottie/lottie.dart';
import 'package:paqufinal/config/theme.dart';
import 'package:paqufinal/screens/onboarding_screen.dart';
import 'package:google_fonts/google_fonts.dart';

class WelcomeScreen extends StatefulWidget {
  const WelcomeScreen({super.key});

  @override
  State<WelcomeScreen> createState() => _WelcomeScreenState();
}

class _WelcomeScreenState extends State<WelcomeScreen> {
  @override
  void initState() {
    super.initState();
    _navigateToOnboarding();
  }

  void _navigateToOnboarding() async {
    await Future.delayed(3000.ms); // 3 segundos de splash
    if (mounted) {
      Navigator.pushReplacement(
        context,
        PageRouteBuilder(
          pageBuilder: (_, __, ___) => const OnboardingScreen(),
          transitionsBuilder: (_, animation, __, child) {
            return FadeTransition(
              opacity: animation,
              child: child,
            );
          },
          transitionDuration: 800.ms,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    ScreenUtil.init(context, designSize: const Size(375, 812));

    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [AppTheme.primaryColor, AppTheme.primaryLight],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: Stack(
          children: [
            // Fondos animados
            Positioned(
              top: -50,
              right: -50,
              child: Container(
                width: 200.w,
                height: 200.h,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: AppTheme.accentColor.withOpacity(0.1),
                ),
              ),
            ).animate().fadeIn(delay: 200.ms),
            
            Positioned(
              bottom: -80,
              left: -80,
              child: Container(
                width: 250.w,
                height: 250.h,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: AppTheme.secondaryColor.withOpacity(0.1),
                ),
              ),
            ).animate().fadeIn(delay: 400.ms),

            Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  // Logo/Animación Lottie
                  Container(
                    width: 180.w,
                    height: 180.h,
                    decoration: BoxDecoration(
                      color: Colors.white,
                      shape: BoxShape.circle,
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.1),
                          blurRadius: 20,
                          spreadRadius: 5,
                        ),
                      ],
                    ),
                    child: Lottie.asset(
                      'assets/animations/llama_welcome.json',
                      fit: BoxFit.contain,
                    ),
                  )
                  .animate()
                  .scale(duration: 1000.ms, curve: Curves.elasticOut)
                  .fadeIn(duration: 800.ms),

                  SizedBox(height: 40.h),

                  // Título
                  Text(
                    'PAQU',
                    style: GoogleFonts.patrickHand(
                      fontSize: 48.sp,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                      shadows: [
                        Shadow(
                          blurRadius: 10,
                          color: Colors.black.withOpacity(0.2),
                          offset: const Offset(2, 2),
                        ),
                      ],
                    ),
                  )
                  .animate()
                  .fadeIn(delay: 600.ms)
                  .slideY(begin: 0.5, end: 0),

                  SizedBox(height: 8.h),

                  // Subtítulo
                  Text(
                    'Aprende Quechua Divirtiéndote',
                    style: GoogleFonts.comicNeue(
                      fontSize: 18.sp,
                      color: Colors.white.withOpacity(0.9),
                      fontWeight: FontWeight.w500,
                    ),
                  )
                  .animate()
                  .fadeIn(delay: 800.ms)
                  .slideY(begin: 0.5, end: 0),

                  SizedBox(height: 60.h),

                  // Loading indicator
                  Container(
                    width: 40.w,
                    height: 40.h,
                    padding: EdgeInsets.all(8.w),
                    child: CircularProgressIndicator(
                      strokeWidth: 3,
                      valueColor: AlwaysStoppedAnimation<Color>(Colors.white.withOpacity(0.8)),
                    ),
                  )
                  .animate()
                  .fadeIn(delay: 1000.ms)
                  .scale(delay: 1000.ms),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
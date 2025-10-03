import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:lottie/lottie.dart';
import 'package:paqufinal/config/theme.dart';
import 'package:paqufinal/screens/login_screen.dart';
import 'package:paqufinal/screens/register_intro_screen.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:smooth_page_indicator/smooth_page_indicator.dart';

class OnboardingScreen extends StatefulWidget {
  const OnboardingScreen({super.key});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {
  final PageController _pageController = PageController();
  int _currentPage = 0;

  final List<OnboardingPage> _pages = [
    OnboardingPage(
      title: '¡Bienvenido a Paqu!',
      description: 'Aprende Quechua de manera divertida y efectiva',
      lottieAsset: 'assets/animations/welcome_llama.json',
      color: AppTheme.primaryColor,
    ),
    OnboardingPage(
      title: 'Aprende Jugando',
      description: 'Lecciones interactivas que parecen juegos',
      lottieAsset: 'assets/animations/learning.json',
      color: AppTheme.secondaryColor,
    ),
    OnboardingPage(
      title: 'Logra tus Metas',
      description: 'Establece metas diarias y mantén tu racha',
      lottieAsset: 'assets/animations/achievement.json',
      color: AppTheme.accentColor,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    ScreenUtil.init(context, designSize: const Size(375, 812));

    return Scaffold(
      body: Stack(
        children: [
          // Fondo con gradiente dinámico
          AnimatedContainer(
            duration: 500.ms,
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [
                  _pages[_currentPage].color,
                  _pages[_currentPage].color.withOpacity(0.8),
                ],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
            ),
          ),

          // Contenido principal
          Column(
            children: [
              Expanded(
                child: PageView.builder(
                  controller: _pageController,
                  itemCount: _pages.length,
                  onPageChanged: (index) {
                    setState(() => _currentPage = index);
                  },
                  itemBuilder: (context, index) {
                    return _buildPage(_pages[index]);
                  },
                ),
              ),

              // Indicador de páginas
              SmoothPageIndicator(
                controller: _pageController,
                count: _pages.length,
                effect: ExpandingDotsEffect(
                  activeDotColor: Colors.white,
                  dotColor: Colors.white.withOpacity(0.5),
                  dotHeight: 8.h,
                  dotWidth: 8.w,
                  spacing: 8.w,
                  expansionFactor: 3,
                ),
              ).animate().fadeIn(delay: 200.ms),

              SizedBox(height: 40.h),

              // Botones de acción
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 24.w),
                child: Column(
                  children: [
                    // Botón "Ya tengo cuenta"
                    _buildActionButton(
                      text: '¡Sí, ya tengo una cuenta!',
                      backgroundColor: Colors.white,
                      textColor: _pages[_currentPage].color,
                      onPressed: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(builder: (_) => const LoginScreen()),
                        );
                      },
                    ).animate().fadeIn(delay: 400.ms).slideY(begin: 0.5, end: 0),

                    SizedBox(height: 16.h),

                    // Botón "Crear cuenta"
                    _buildActionButton(
                      text: 'No, quiero crear una cuenta',
                      backgroundColor: Colors.transparent,
                      textColor: Colors.white,
                      borderColor: Colors.white,
                      onPressed: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(builder: (_) => const RegisterIntroScreen()),
                        );
                      },
                    ).animate().fadeIn(delay: 600.ms).slideY(begin: 0.5, end: 0),

                    SizedBox(height: 30.h),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildPage(OnboardingPage page) {
    return Padding(
      padding: EdgeInsets.all(24.w),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Animación Lottie
          Container(
            width: 250.w,
            height: 250.h,
            child: Lottie.asset(
              page.lottieAsset,
              fit: BoxFit.contain,
            ),
          )
          .animate()
          .scale(duration: 600.ms, curve: Curves.elasticOut)
          .fadeIn(duration: 500.ms),

          SizedBox(height: 40.h),

          // Título
          Text(
            page.title,
            style: GoogleFonts.patrickHand(
              fontSize: 32.sp,
              fontWeight: FontWeight.bold,
              color: Colors.white,
              height: 1.2,
            ),
            textAlign: TextAlign.center,
          )
          .animate()
          .fadeIn(delay: 200.ms)
          .slideY(begin: 0.3, end: 0),

          SizedBox(height: 16.h),

          // Descripción
          Text(
            page.description,
            style: GoogleFonts.comicNeue(
              fontSize: 18.sp,
              color: Colors.white.withOpacity(0.9),
              height: 1.4,
            ),
            textAlign: TextAlign.center,
          )
          .animate()
          .fadeIn(delay: 400.ms)
          .slideY(begin: 0.3, end: 0),
        ],
      ),
    );
  }

  Widget _buildActionButton({
    required String text,
    required Color backgroundColor,
    required Color textColor,
    Color? borderColor,
    required VoidCallback onPressed,
  }) {
    return SizedBox(
      width: double.infinity,
      height: 56.h,
      child: ElevatedButton(
        onPressed: onPressed,
        style: ElevatedButton.styleFrom(
          backgroundColor: backgroundColor,
          foregroundColor: textColor,
          elevation: borderColor == null ? 8 : 0,
          shadowColor: borderColor == null ? Colors.black.withOpacity(0.2) : null,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(28.h),
            side: borderColor != null
                ? BorderSide(color: borderColor, width: 2)
                : BorderSide.none,
          ),
        ),
        child: Text(
          text,
          style: GoogleFonts.comicNeue(
            fontSize: 18.sp,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}

class OnboardingPage {
  final String title;
  final String description;
  final String lottieAsset;
  final Color color;

  OnboardingPage({
    required this.title,
    required this.description,
    required this.lottieAsset,
    required this.color,
  });
}
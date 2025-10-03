import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:lottie/lottie.dart';
import 'package:paqufinal/config/theme.dart';
import 'package:paqufinal/screens/register_screen.dart';
import 'package:google_fonts/google_fonts.dart';

class RegisterIntroScreen extends StatefulWidget {
  const RegisterIntroScreen({super.key});

  @override
  State<RegisterIntroScreen> createState() => _RegisterIntroScreenState();
}

class _RegisterIntroScreenState extends State<RegisterIntroScreen> {
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
            // Elementos decorativos
            Positioned(
              top: 50.h,
              left: 20.w,
              child: _buildFloatingShape(
                size: 80.w,
                color: AppTheme.accentColor.withOpacity(0.2),
              ),
            ).animate().fadeIn(delay: 200.ms).slideX(begin: -1),

            Positioned(
              bottom: 100.h,
              right: 30.w,
              child: _buildFloatingShape(
                size: 60.w,
                color: AppTheme.secondaryColor.withOpacity(0.2),
              ),
            ).animate().fadeIn(delay: 400.ms).slideX(begin: 1),

            Column(
              children: [
                SizedBox(height: 80.h),

                // Animación de la llama
                Container(
                  width: 200.w,
                  height: 200.h,
                  child: Lottie.asset(
                    'assets/animations/llama_tecleando.json',
                    fit: BoxFit.contain,
                  ),
                )
                .animate()
                .scale(duration: 800.ms, curve: Curves.elasticOut)
                .fadeIn(duration: 600.ms),

                SizedBox(height: 40.h),

                // Título principal
                Padding(
                  padding: EdgeInsets.symmetric(horizontal: 40.w),
                  child: Text(
                    '¡Vamos a crear tu perfil juntos!',
                    style: GoogleFonts.patrickHand(
                      fontSize: 32.sp,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                      height: 1.2,
                    ),
                    textAlign: TextAlign.center,
                  ),
                )
                .animate()
                .fadeIn(delay: 400.ms)
                .slideY(begin: 0.3, end: 0),

                SizedBox(height: 20.h),

                // Descripción
                Padding(
                  padding: EdgeInsets.symmetric(horizontal: 40.w),
                  child: Text(
                    'Solo necesitamos algunos datos para personalizar tu experiencia de aprendizaje',
                    style: GoogleFonts.comicNeue(
                      fontSize: 16.sp,
                      color: Colors.white.withOpacity(0.9),
                      height: 1.4,
                    ),
                    textAlign: TextAlign.center,
                  ),
                )
                .animate()
                .fadeIn(delay: 600.ms)
                .slideY(begin: 0.3, end: 0),

                const Spacer(),

                // Lista de beneficios
                _buildBenefitsList(),

                const Spacer(),

                // Botón de empezar
                Padding(
                  padding: EdgeInsets.symmetric(horizontal: 24.w, vertical: 40.h),
                  child: _buildStartButton(),
                )
                .animate()
                .fadeIn(delay: 1000.ms)
                .slideY(begin: 0.5, end: 0),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFloatingShape({required double size, required Color color}) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: color,
      ),
    );
  }

  Widget _buildBenefitsList() {
    final benefits = [
      '🎯 Metas personalizadas',
      '📊 Seguimiento de progreso',
      '🏆 Logros y recompensas',
      '👥 Comunidad de aprendizaje',
    ];

    return Column(
      children: benefits.map((benefit) {
        return Padding(
          padding: EdgeInsets.symmetric(vertical: 8.h, horizontal: 40.w),
          child: Row(
            children: [
              Container(
                width: 24.w,
                height: 24.h,
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.2),
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  Icons.check,
                  size: 14.w,
                  color: Colors.white,
                ),
              ),
              SizedBox(width: 16.w),
              Expanded(
                child: Text(
                  benefit,
                  style: GoogleFonts.comicNeue(
                    fontSize: 16.sp,
                    color: Colors.white,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),
            ],
          )
          .animate()
          .fadeIn(delay: (800 + benefits.indexOf(benefit) * 100).ms)
          .slideX(begin: -0.5, end: 0),
        );
      }).toList(),
    );
  }

  Widget _buildStartButton() {
    return SizedBox(
      width: double.infinity,
      height: 60.h,
      child: ElevatedButton(
        onPressed: () {
          Navigator.push(
            context,
            PageRouteBuilder(
              pageBuilder: (_, __, ___) => const RegisterScreen(),
              transitionsBuilder: (_, animation, __, child) {
                return SlideTransition(
                  position: Tween<Offset>(
                    begin: const Offset(1.0, 0.0),
                    end: Offset.zero,
                  ).animate(CurvedAnimation(
                    parent: animation,
                    curve: Curves.easeInOut,
                  )),
                  child: child,
                );
              },
              transitionDuration: 500.ms,
            ),
          );
        },
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.white,
          foregroundColor: AppTheme.primaryColor,
          elevation: 12,
          shadowColor: Colors.black.withOpacity(0.3),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(30.h),
          ),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              'Empezar Ahora',
              style: GoogleFonts.comicNeue(
                fontSize: 20.sp,
                fontWeight: FontWeight.bold,
              ),
            ),
            SizedBox(width: 12.w),
            Icon(
              Icons.arrow_forward_rounded,
              size: 24.w,
            ),
          ],
        ),
      ),
    );
  }
}
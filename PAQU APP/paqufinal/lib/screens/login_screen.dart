import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:lottie/lottie.dart';
import 'package:paqufinal/config/theme.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:paqufinal/services/auth_service.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final AuthService _authService = AuthService();
  bool _isLoading = false;
  bool _obscurePassword = true;

  @override
  Widget build(BuildContext context) {
    ScreenUtil.init(context, designSize: const Size(375, 812));

    return Scaffold(
      backgroundColor: AppTheme.backgroundColor,
      body: SingleChildScrollView(
        child: Stack(
          children: [
            // Fondos decorativos
            Positioned(
              top: -50.h,
              right: -50.w,
              child: Container(
                width: 200.w,
                height: 200.h,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      AppTheme.primaryColor.withOpacity(0.1),
                      AppTheme.primaryColor.withOpacity(0.05),
                    ],
                  ),
                ),
              ),
            ),

            Positioned(
              bottom: -80.h,
              left: -80.w,
              child: Container(
                width: 250.w,
                height: 250.h,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      AppTheme.secondaryColor.withOpacity(0.1),
                      AppTheme.secondaryColor.withOpacity(0.05),
                    ],
                  ),
                ),
              ),
            ),

            Column(
              children: [
                // Header con animación
                Container(
                  height: 280.h,
                  decoration: BoxDecoration(
                    gradient: AppTheme.primaryGradient,
                    borderRadius: BorderRadius.only(
                      bottomLeft: Radius.circular(40.r),
                      bottomRight: Radius.circular(40.r),
                    ),
                  ),
                  child: Stack(
                    children: [
                      Positioned(
                        top: 50.h,
                        left: 0,
                        right: 0,
                        child: Column(
                          children: [
                            // Animación Lottie
                            Container(
                              width: 120.w,
                              height: 120.h,
                              child: Lottie.asset(
                                'assets/animations/llama_estudiando.json',
                                fit: BoxFit.contain,
                              ),
                            )
                            .animate()
                            .scale(duration: 800.ms, curve: Curves.elasticOut),

                            SizedBox(height: 16.h),

                            // Título
                            Text(
                              '¡Bienvenido de nuevo!',
                              style: GoogleFonts.patrickHand(
                                fontSize: 28.sp,
                                fontWeight: FontWeight.bold,
                                color: Colors.white,
                              ),
                            )
                            .animate()
                            .fadeIn(delay: 300.ms)
                            .slideY(begin: 0.5, end: 0),

                            SizedBox(height: 8.h),

                            Text(
                              'Ingresa a tu cuenta',
                              style: GoogleFonts.comicNeue(
                                fontSize: 16.sp,
                                color: Colors.white.withOpacity(0.9),
                              ),
                            )
                            .animate()
                            .fadeIn(delay: 500.ms)
                            .slideY(begin: 0.5, end: 0),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),

                // Formulario
                Padding(
                  padding: EdgeInsets.all(24.w),
                  child: Form(
                    key: _formKey,
                    child: Column(
                      children: [
                        SizedBox(height: 20.h),

                        // Campo de email
                        _buildEmailField()
                            .animate()
                            .fadeIn(delay: 200.ms)
                            .slideX(begin: -30, end: 0),

                        SizedBox(height: 20.h),

                        // Campo de contraseña
                        _buildPasswordField()
                            .animate()
                            .fadeIn(delay: 400.ms)
                            .slideX(begin: -30, end: 0),

                        SizedBox(height: 16.h),

                        // Olvidé contraseña
                        Align(
                          alignment: Alignment.centerRight,
                          child: TextButton(
                            onPressed: () {
                              // TODO: Implementar recuperación de contraseña
                            },
                            child: Text(
                              '¿Olvidaste tu contraseña?',
                              style: GoogleFonts.comicNeue(
                                fontSize: 14.sp,
                                color: AppTheme.secondaryColor,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ),
                        )
                        .animate()
                        .fadeIn(delay: 600.ms),

                        SizedBox(height: 24.h),

                        // Botón de inicio de sesión
                        _buildLoginButton()
                            .animate()
                            .fadeIn(delay: 800.ms)
                            .slideY(begin: 30, end: 0),

                        SizedBox(height: 32.h),

                        // Divisor
                        _buildDivider()
                            .animate()
                            .fadeIn(delay: 1000.ms),

                        SizedBox(height: 24.h),

                        // Botones de login social
                        _buildSocialButtons()
                            .animate()
                            .fadeIn(delay: 1200.ms)
                            .slideY(begin: 30, end: 0),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEmailField() {
    return TextFormField(
      controller: _emailController,
      keyboardType: TextInputType.emailAddress,
      decoration: InputDecoration(
        hintText: 'correo@ejemplo.com',
        labelText: 'Correo electrónico',
        prefixIcon: Container(
          width: 20.w,
          height: 20.h,
          margin: EdgeInsets.all(12.w),
          child: Icon(
            Icons.email_rounded,
            color: AppTheme.primaryColor,
            size: 20.w,
          ),
        ),
        floatingLabelBehavior: FloatingLabelBehavior.always,
      ),
      validator: (value) {
        if (value == null || value.isEmpty) {
          return 'Por favor ingresa tu correo';
        }
        if (!RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(value)) {
          return 'Ingresa un correo válido';
        }
        return null;
      },
    );
  }

  Widget _buildPasswordField() {
    return TextFormField(
      controller: _passwordController,
      obscureText: _obscurePassword,
      decoration: InputDecoration(
        hintText: '••••••••',
        labelText: 'Contraseña',
        prefixIcon: Container(
          width: 20.w,
          height: 20.h,
          margin: EdgeInsets.all(12.w),
          child: Icon(
            Icons.lock_rounded,
            color: AppTheme.primaryColor,
            size: 20.w,
          ),
        ),
        suffixIcon: IconButton(
          icon: Icon(
            _obscurePassword ? Icons.visibility_rounded : Icons.visibility_off_rounded,
            color: AppTheme.textSecondary,
          ),
          onPressed: () {
            setState(() => _obscurePassword = !_obscurePassword);
          },
        ),
        floatingLabelBehavior: FloatingLabelBehavior.always,
      ),
      validator: (value) {
        if (value == null || value.isEmpty) {
          return 'Por favor ingresa tu contraseña';
        }
        if (value.length < 6) {
          return 'La contraseña debe tener al menos 6 caracteres';
        }
        return null;
      },
    );
  }

  Widget _buildLoginButton() {
    return SizedBox(
      width: double.infinity,
      height: 56.h,
      child: ElevatedButton(
        onPressed: _isLoading ? null : _handleLogin,
        style: ElevatedButton.styleFrom(
          backgroundColor: AppTheme.primaryColor,
          foregroundColor: Colors.white,
          elevation: 8,
          shadowColor: AppTheme.primaryColor.withOpacity(0.3),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(28.h),
          ),
        ),
        child: _isLoading
            ? SizedBox(
                width: 24.w,
                height: 24.h,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                ),
              )
            : Text(
                'Iniciar Sesión',
                style: GoogleFonts.comicNeue(
                  fontSize: 18.sp,
                  fontWeight: FontWeight.bold,
                ),
              ),
      ),
    );
  }

  Widget _buildDivider() {
    return Row(
      children: [
        Expanded(
          child: Divider(
            color: AppTheme.textDisabled.withOpacity(0.3),
            thickness: 1,
          ),
        ),
        Padding(
          padding: EdgeInsets.symmetric(horizontal: 16.w),
          child: Text(
            'o continúa con',
            style: GoogleFonts.comicNeue(
              fontSize: 14.sp,
              color: AppTheme.textSecondary,
            ),
          ),
        ),
        Expanded(
          child: Divider(
            color: AppTheme.textDisabled.withOpacity(0.3),
            thickness: 1,
          ),
        ),
      ],
    );
  }

  Widget _buildSocialButtons() {
    return Row(
      children: [
        Expanded(
          child: _buildSocialButton(
            icon: 'assets/icons/google.png',
            text: 'Google',
            onPressed: () {
              // TODO: Implementar login con Google
            },
          ),
        ),
        SizedBox(width: 16.w),
        Expanded(
          child: _buildSocialButton(
            icon: 'assets/icons/facebook.png',
            text: 'Facebook',
            onPressed: () {
              // TODO: Implementar login con Facebook
            },
          ),
        ),
      ],
    );
  }

  Widget _buildSocialButton({
    required String icon,
    required String text,
    required VoidCallback onPressed,
  }) {
    return SizedBox(
      height: 56.h,
      child: OutlinedButton(
        onPressed: onPressed,
        style: OutlinedButton.styleFrom(
          foregroundColor: AppTheme.textPrimary,
          side: BorderSide(
            color: AppTheme.textDisabled.withOpacity(0.3),
            width: 1.5,
          ),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16.r),
          ),
          backgroundColor: Colors.transparent,
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Image.asset(
              icon,
              width: 24.w,
              height: 24.h,
              color: AppTheme.textPrimary,
            ),
            SizedBox(width: 8.w),
            Text(
              text,
              style: GoogleFonts.comicNeue(
                fontSize: 14.sp,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _handleLogin() async {
    if (_formKey.currentState!.validate()) {
      setState(() => _isLoading = true);

      final user = await _authService.signInWithEmailAndPassword(
        _emailController.text.trim(),
        _passwordController.text.trim(),
      );

      setState(() => _isLoading = false);

      if (user != null) {
        _showSuccessAnimation();
        await Future.delayed(1500.ms);
        // TODO: Navegar a HomeScreen
        // Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => HomeScreen()));
      } else {
        _showErrorDialog();
      }
    }
  }

  void _showSuccessAnimation() {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => Dialog(
        backgroundColor: Colors.transparent,
        elevation: 0,
        child: Container(
          width: 200.w,
          height: 200.h,
          child: Lottie.asset(
            'assets/animations/success.json',
            fit: BoxFit.contain,
          ),
        ),
      ),
    );
  }

  void _showErrorDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20.r),
        ),
        title: Text(
          'Error al iniciar sesión',
          style: GoogleFonts.patrickHand(
            fontSize: 20.sp,
            color: AppTheme.errorColor,
          ),
        ),
        content: Text(
          'Verifica tus credenciales e intenta nuevamente.',
          style: GoogleFonts.comicNeue(
            fontSize: 16.sp,
            color: AppTheme.textSecondary,
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text(
              'Entendido',
              style: GoogleFonts.comicNeue(
                fontSize: 16.sp,
                color: AppTheme.primaryColor,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }
}
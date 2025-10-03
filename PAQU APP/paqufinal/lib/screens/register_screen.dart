import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:paqufinal/config/theme.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:paqufinal/services/auth_service.dart';
import 'package:intl/intl.dart';
import 'package:country_picker/country_picker.dart';
import 'package:firebase_auth/firebase_auth.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  final AuthService _authService = AuthService();
  
  DateTime? _selectedDate;
  Country? _selectedCountry;
  bool _isLoading = false;
  bool _obscurePassword = true;
  bool _obscureConfirmPassword = true;

  final List<String> _nacionalidades = [
    'Peruana', 'Argentina', 'Boliviana', 'Chilena', 'Colombiana', 
    'Ecuatoriana', 'Española', 'Mexicana', 'Otro'
  ];

  @override
  Widget build(BuildContext context) {
    ScreenUtil.init(context, designSize: const Size(375, 812));

    return Scaffold(
      backgroundColor: AppTheme.backgroundColor,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(
            Icons.arrow_back_rounded,
            color: AppTheme.textPrimary,
            size: 24.w,
          ),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: EdgeInsets.all(24.w),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Título
                Text(
                  'Crear Cuenta',
                  style: GoogleFonts.patrickHand(
                    fontSize: 32.sp,
                    fontWeight: FontWeight.bold,
                    color: AppTheme.textPrimary,
                    height: 1.2,
                  ),
                )
                .animate()
                .fadeIn(duration: 500.ms)
                .slideX(begin: -0.5, end: 0),

                SizedBox(height: 8.h),

                Text(
                  'Completa tus datos para empezar',
                  style: GoogleFonts.comicNeue(
                    fontSize: 16.sp,
                    color: AppTheme.textSecondary,
                  ),
                )
                .animate()
                .fadeIn(delay: 200.ms)
                .slideX(begin: -0.5, end: 0),

                SizedBox(height: 32.h),

                // Campos del formulario
                _buildNameField()
                    .animate()
                    .fadeIn(delay: 300.ms)
                    .slideX(begin: -30, end: 0),

                SizedBox(height: 20.h),

                _buildBirthDateField()
                    .animate()
                    .fadeIn(delay: 400.ms)
                    .slideX(begin: -30, end: 0),

                SizedBox(height: 20.h),

                _buildNationalityField()
                    .animate()
                    .fadeIn(delay: 500.ms)
                    .slideX(begin: -30, end: 0),

                SizedBox(height: 20.h),

                _buildEmailField()
                    .animate()
                    .fadeIn(delay: 600.ms)
                    .slideX(begin: -30, end: 0),

                SizedBox(height: 20.h),

                _buildPasswordField()
                    .animate()
                    .fadeIn(delay: 700.ms)
                    .slideX(begin: -30, end: 0),

                SizedBox(height: 20.h),

                _buildConfirmPasswordField()
                    .animate()
                    .fadeIn(delay: 800.ms)
                    .slideX(begin: -30, end: 0),

                SizedBox(height: 32.h),

                // Términos y condiciones
                _buildTermsCheckbox()
                    .animate()
                    .fadeIn(delay: 900.ms)
                    .slideY(begin: 0.5, end: 0),

                SizedBox(height: 24.h),

                // Botón de registro
                _buildRegisterButton()
                    .animate()
                    .fadeIn(delay: 1000.ms)
                    .slideY(begin: 0.5, end: 0),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildNameField() {
    return TextFormField(
      controller: _nameController,
      decoration: InputDecoration(
        hintText: 'Ej: María González',
        labelText: 'Nombre completo',
        prefixIcon: Container(
          width: 20.w,
          height: 20.h,
          margin: EdgeInsets.all(12.w),
          child: Icon(
            Icons.person_rounded,
            color: AppTheme.primaryColor,
            size: 20.w,
          ),
        ),
        floatingLabelBehavior: FloatingLabelBehavior.always,
      ),
      validator: (value) {
        if (value == null || value.isEmpty) {
          return 'Por favor ingresa tu nombre';
        }
        if (value.length < 2) {
          return 'El nombre debe tener al menos 2 caracteres';
        }
        return null;
      },
    );
  }

  Widget _buildBirthDateField() {
    return TextFormField(
      readOnly: true,
      decoration: InputDecoration(
        hintText: 'Selecciona tu fecha de nacimiento',
        labelText: 'Fecha de nacimiento',
        prefixIcon: Container(
          width: 20.w,
          height: 20.h,
          margin: EdgeInsets.all(12.w),
          child: Icon(
            Icons.cake_rounded,
            color: AppTheme.primaryColor,
            size: 20.w,
          ),
        ),
        floatingLabelBehavior: FloatingLabelBehavior.always,
        suffixIcon: Icon(
          Icons.calendar_today_rounded,
          color: AppTheme.textSecondary,
        ),
      ),
      controller: TextEditingController(
        text: _selectedDate != null
            ? DateFormat('dd/MM/yyyy').format(_selectedDate!)
            : '',
      ),
      onTap: _selectBirthDate,
      validator: (value) {
        if (_selectedDate == null) {
          return 'Por favor selecciona tu fecha de nacimiento';
        }
        final now = DateTime.now();
        final age = now.difference(_selectedDate!).inDays ~/ 365;
        if (age < 12) {
          return 'Debes tener al menos 12 años';
        }
        if (age > 100) {
          return 'Por favor ingresa una fecha válida';
        }
        return null;
      },
    );
  }

  Widget _buildNationalityField() {
    return TextFormField(
      readOnly: true,
      decoration: InputDecoration(
        hintText: '¿De dónde eres?',
        labelText: 'Nacionalidad',
        prefixIcon: Container(
          width: 20.w,
          height: 20.h,
          margin: EdgeInsets.all(12.w),
          child: Icon(
            Icons.flag_rounded,
            color: AppTheme.primaryColor,
            size: 20.w,
          ),
        ),
        floatingLabelBehavior: FloatingLabelBehavior.always,
        suffixIcon: Icon(
          Icons.arrow_drop_down_rounded,
          color: AppTheme.textSecondary,
        ),
      ),
      controller: TextEditingController(
        text: _selectedCountry?.name ?? '',
      ),
      onTap: _showCountryPicker,
      validator: (value) {
        if (_selectedCountry == null) {
          return 'Por favor selecciona tu nacionalidad';
        }
        return null;
      },
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

  Widget _buildConfirmPasswordField() {
    return TextFormField(
      controller: _confirmPasswordController,
      obscureText: _obscureConfirmPassword,
      decoration: InputDecoration(
        hintText: '••••••••',
        labelText: 'Confirmar contraseña',
        prefixIcon: Container(
          width: 20.w,
          height: 20.h,
          margin: EdgeInsets.all(12.w),
          child: Icon(
            Icons.lock_outline_rounded,
            color: AppTheme.primaryColor,
            size: 20.w,
          ),
        ),
        suffixIcon: IconButton(
          icon: Icon(
            _obscureConfirmPassword ? Icons.visibility_rounded : Icons.visibility_off_rounded,
            color: AppTheme.textSecondary,
          ),
          onPressed: () {
            setState(() => _obscureConfirmPassword = !_obscureConfirmPassword);
          },
        ),
        floatingLabelBehavior: FloatingLabelBehavior.always,
      ),
      validator: (value) {
        if (value == null || value.isEmpty) {
          return 'Por favor confirma tu contraseña';
        }
        if (value != _passwordController.text) {
          return 'Las contraseñas no coinciden';
        }
        return null;
      },
    );
  }

  Widget _buildTermsCheckbox() {
    return Row(
      children: [
        Container(
          width: 20.w,
          height: 20.h,
          decoration: BoxDecoration(
            color: AppTheme.primaryColor,
            borderRadius: BorderRadius.circular(4.r),
          ),
          child: Icon(
            Icons.check_rounded,
            size: 16.w,
            color: Colors.white,
          ),
        ),
        SizedBox(width: 12.w),
        Expanded(
          child: RichText(
            text: TextSpan(
              text: 'Acepto los ',
              style: GoogleFonts.comicNeue(
                fontSize: 14.sp,
                color: AppTheme.textSecondary,
              ),
              children: [
                TextSpan(
                  text: 'Términos y Condiciones',
                  style: GoogleFonts.comicNeue(
                    fontSize: 14.sp,
                    color: AppTheme.primaryColor,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                TextSpan(
                  text: ' y la ',
                  style: GoogleFonts.comicNeue(
                    fontSize: 14.sp,
                    color: AppTheme.textSecondary,
                  ),
                ),
                TextSpan(
                  text: 'Política de Privacidad',
                  style: GoogleFonts.comicNeue(
                    fontSize: 14.sp,
                    color: AppTheme.primaryColor,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildRegisterButton() {
    return SizedBox(
      width: double.infinity,
      height: 56.h,
      child: ElevatedButton(
        onPressed: _isLoading ? null : _handleRegister,
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
            : Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    'Crear Cuenta',
                    style: GoogleFonts.comicNeue(
                      fontSize: 18.sp,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  SizedBox(width: 12.w),
                  Icon(Icons.arrow_forward_rounded, size: 20.w),
                ],
              ),
      ),
    );
  }

  Future<void> _selectBirthDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: DateTime.now().subtract(const Duration(days: 365 * 16)),
      firstDate: DateTime(1900),
      lastDate: DateTime.now(),
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: ColorScheme.light(
              primary: AppTheme.primaryColor,
              onPrimary: Colors.white,
              onSurface: AppTheme.textPrimary,
            ),
          ),
          child: child!,
        );
      },
    );
    
    if (picked != null && picked != _selectedDate) {
      setState(() => _selectedDate = picked);
    }
  }

  void _showCountryPicker() {
    showCountryPicker(
      context: context,
      showPhoneCode: false,
      onSelect: (Country country) {
        setState(() => _selectedCountry = country);
      },
      countryListTheme: CountryListThemeData(
        borderRadius: BorderRadius.only(
          topLeft: Radius.circular(20.r),
          topRight: Radius.circular(20.r),
        ),
        inputDecoration: InputDecoration(
          hintText: 'Buscar país...',
          prefixIcon: Icon(Icons.search_rounded, color: AppTheme.primaryColor),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(15.r),
            borderSide: BorderSide(color: AppTheme.primaryColor),
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(15.r),
            borderSide: BorderSide(color: AppTheme.primaryColor, width: 2),
          ),
        ),
      ),
    );
  }

  Future<void> _handleRegister() async {
    if (_formKey.currentState!.validate()) {
      setState(() => _isLoading = true);

      final user = await _authService.registerWithEmailAndPassword(
        _emailController.text.trim(),
        _passwordController.text.trim(),
      );

      setState(() => _isLoading = false);

      if (user != null) {
        _showWelcomeScreen(user);
      } else {
        _showErrorDialog();
      }
    }
  }

  void _showWelcomeScreen(User user) {
    // TODO: Navegar a pantalla de bienvenida personalizada
    // Navigator.pushReplacement(
    //   context,
    //   MaterialPageRoute(
    //     builder: (_) => WelcomeUserScreen(user: user, userName: _nameController.text),
    //   ),
    // );
  }

  void _showErrorDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20.r),
        ),
        title: Text(
          'Error al crear cuenta',
          style: GoogleFonts.patrickHand(
            fontSize: 20.sp,
            color: AppTheme.errorColor,
          ),
        ),
        content: Text(
          'El correo electrónico ya está en uso o hay un problema de conexión.',
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
    _nameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }
}
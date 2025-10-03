import 'package:firebase_auth/firebase_auth.dart';
import 'firestore_service.dart';

class AuthService {
  final FirebaseAuth _auth = FirebaseAuth.instance;
  final FirestoreService _firestoreService = FirestoreService();

  // Stream para cambios de estado del usuario
  Stream<User?> get userChanges => _auth.authStateChanges();

  // Registrar usuario con manejo completo de errores
  Future<User?> registerWithEmailAndPassword(String email, String password) async {
    try {
      print('🔄 Intentando registrar usuario: $email');
      
      UserCredential result = await _auth.createUserWithEmailAndPassword(
        email: email, 
        password: password,
      );
      
      print('✅ Usuario registrado en Auth: ${result.user?.uid}');
      
      // Crear perfil de usuario en Firestore
      if (result.user != null) {
        try {
          await _firestoreService.createUserProfile(result.user!);
          print('✅ Perfil de usuario creado en Firestore');
        } catch (e) {
          print('⚠️ Error creando perfil en Firestore: $e');
          // No lanzamos excepción porque el usuario ya se creó en Auth
        }
      }
      
      return result.user;
    } on FirebaseAuthException catch (e) {
      print('❌ Error Firebase Auth: ${e.code} - ${e.message}');
      
      String errorMessage = 'Error al crear cuenta';
      switch (e.code) {
        case 'email-already-in-use':
          errorMessage = 'Este correo ya está registrado';
          break;
        case 'invalid-email':
          errorMessage = 'Correo electrónico inválido';
          break;
        case 'operation-not-allowed':
          errorMessage = 'Operación no permitida';
          break;
        case 'weak-password':
          errorMessage = 'La contraseña es muy débil';
          break;
        default:
          errorMessage = 'Error desconocido: ${e.code}';
      }
      
      throw AuthException(errorMessage);
    } catch (e) {
      print('❌ Error inesperado: $e');
      throw AuthException('Error inesperado: $e');
    }
  }

  // Iniciar sesión con manejo completo de errores
  Future<User?> signInWithEmailAndPassword(String email, String password) async {
    try {
      print('🔄 Intentando iniciar sesión: $email');
      
      UserCredential result = await _auth.signInWithEmailAndPassword(
        email: email, 
        password: password,
      );
      
      print('✅ Sesión iniciada: ${result.user?.uid}');
      
      // Actualizar último login
      if (result.user != null) {
        await _updateLastLogin(result.user!);
      }
      
      return result.user;
    } on FirebaseAuthException catch (e) {
      print('❌ Error Firebase Auth: ${e.code} - ${e.message}');
      
      String errorMessage = 'Error al iniciar sesión';
      switch (e.code) {
        case 'user-not-found':
          errorMessage = 'No existe una cuenta con este correo';
          break;
        case 'wrong-password':
          errorMessage = 'Contraseña incorrecta';
          break;
        case 'invalid-email':
          errorMessage = 'Correo electrónico inválido';
          break;
        case 'user-disabled':
          errorMessage = 'Esta cuenta ha sido desactivada';
          break;
        case 'too-many-requests':
          errorMessage = 'Demasiados intentos. Intenta más tarde';
          break;
        default:
          errorMessage = 'Error desconocido: ${e.code}';
      }
      
      throw AuthException(errorMessage);
    } catch (e) {
      print('❌ Error inesperado: $e');
      throw AuthException('Error inesperado: $e');
    }
  }

  // Cerrar sesión
  Future<void> signOut() async {
    try {
      await _auth.signOut();
      print('✅ Sesión cerrada correctamente');
    } catch (e) {
      print('❌ Error al cerrar sesión: $e');
      throw AuthException('Error al cerrar sesión');
    }
  }

  // Enviar email de verificación
  Future<void> sendEmailVerification() async {
    try {
      final user = _auth.currentUser;
      if (user != null && !user.emailVerified) {
        await user.sendEmailVerification();
        print('✅ Email de verificación enviado');
      }
    } catch (e) {
      print('❌ Error enviando email de verificación: $e');
      throw AuthException('Error enviando email de verificación');
    }
  }

  // Enviar email de recuperación de contraseña
  Future<void> sendPasswordResetEmail(String email) async {
    try {
      await _auth.sendPasswordResetEmail(email: email);
      print('✅ Email de recuperación enviado a: $email');
    } on FirebaseAuthException catch (e) {
      print('❌ Error enviando email de recuperación: ${e.code}');
      String errorMessage = 'Error enviando email de recuperación';
      if (e.code == 'user-not-found') {
        errorMessage = 'No existe una cuenta con este correo';
      }
      throw AuthException(errorMessage);
    } catch (e) {
      print('❌ Error inesperado: $e');
      throw AuthException('Error enviando email de recuperación');
    }
  }

  // Actualizar último login en Firestore
  Future<void> _updateLastLogin(User user) async {
    try {
      await _firestoreService.updateLastLogin(user.uid);
    } catch (e) {
      print('⚠️ Error actualizando último login: $e');
    }
  }

  // Verificar si el usuario está autenticado
  bool get isUserLoggedIn => _auth.currentUser != null;

  // Obtener usuario actual
  User? get currentUser => _auth.currentUser;

  // Verificar si el email está verificado
  bool get isEmailVerified => _auth.currentUser?.emailVerified ?? false;

  // Obtener ID del usuario actual
  String? get currentUserId => _auth.currentUser?.uid;
}

// Excepción personalizada para errores de autenticación
class AuthException implements Exception {
  final String message;
  AuthException(this.message);

  @override
  String toString() => message;
}
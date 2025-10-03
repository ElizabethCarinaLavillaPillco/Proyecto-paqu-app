import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';

class FirestoreService {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;

  // Nombres de colecciones y campos
  static const String usersCollection = 'users';
  static const String userInfoField = 'userInfo';
  static const String settingsField = 'settings';
  static const String progressField = 'progress';
  static const String achievementsField = 'achievements';

  // ===== MÉTODOS PARA USUARIOS =====

  // Crear perfil de usuario completo
  Future<void> createUserProfile(User user) async {
    try {
      print('🔄 Creando perfil para usuario: ${user.uid}');
      
      final userData = {
        userInfoField: {
          'uid': user.uid,
          'email': user.email,
          'displayName': user.displayName ?? 'Aprendiz Paqu',
          'avatar': 'avatar_default',
          'createdAt': FieldValue.serverTimestamp(),
          'lastLogin': FieldValue.serverTimestamp(),
          'emailVerified': user.emailVerified,
          'isActive': true,
        },
        settingsField: {
          'dailyGoal': 10, // minutos por defecto
          'notifications': true,
          'soundEffects': true,
          'language': 'es',
          'difficulty': 'beginner', // beginner, intermediate, advanced
          'theme': 'light', // light, dark, system
        },
        progressField: {
          'currentDailyProgress': 0,
          'totalMinutes': 0,
          'totalLessons': 0,
          'currentStreak': 0,
          'longestStreak': 0,
          'lastActivityDate': FieldValue.serverTimestamp(),
          'level': 1,
          'experience': 0,
          'coins': 0,
        },
        achievementsField: {
          'unlocked': [],
          'inProgress': ['first_lesson', 'first_streak'],
          'totalAchievements': 0,
        },
      };

      await _firestore
          .collection(usersCollection)
          .doc(user.uid)
          .set(userData, SetOptions(merge: false));
          
      print('✅ Perfil creado exitosamente para: ${user.uid}');
    } catch (e) {
      print('❌ Error creando perfil: $e');
      rethrow;
    }
  }

  // Obtener datos completos del usuario
  Future<Map<String, dynamic>?> getUserData([String? userId]) async {
    try {
      final uid = userId ?? _auth.currentUser?.uid;
      if (uid == null) throw Exception('Usuario no autenticado');

      final doc = await _firestore
          .collection(usersCollection)
          .doc(uid)
          .get();

      if (doc.exists) {
        print('✅ Datos de usuario obtenidos: $uid');
        return doc.data();
      } else {
        print('⚠️ Documento de usuario no encontrado: $uid');
        return null;
      }
    } catch (e) {
      print('❌ Error obteniendo datos de usuario: $e');
      rethrow;
    }
  }

  // Actualizar último login
  Future<void> updateLastLogin(String userId) async {
    try {
      await _firestore
          .collection(usersCollection)
          .doc(userId)
          .update({
        '$userInfoField.lastLogin': FieldValue.serverTimestamp(),
      });
      print('✅ Último login actualizado: $userId');
    } catch (e) {
      print('⚠️ Error actualizando último login: $e');
    }
  }

  // Actualizar meta diaria
  Future<void> updateDailyGoal(int dailyGoal) async {
    try {
      final user = _auth.currentUser;
      if (user == null) throw Exception('Usuario no autenticado');

      await _firestore
          .collection(usersCollection)
          .doc(user.uid)
          .update({
        '$settingsField.dailyGoal': dailyGoal,
        '$userInfoField.lastUpdated': FieldValue.serverTimestamp(),
      });
      print('✅ Meta diaria actualizada: $dailyGoal minutos');
    } catch (e) {
      print('❌ Error actualizando meta diaria: $e');
      rethrow;
    }
  }

  // Actualizar progreso de aprendizaje
  Future<void> updateLearningProgress({
    required int minutesCompleted,
    required int experienceGained,
    required int coinsEarned,
    required String lessonId,
  }) async {
    try {
      final user = _auth.currentUser;
      if (user == null) throw Exception('Usuario no autenticado');

      await _firestore.runTransaction((transaction) async {
        final docRef = _firestore.collection(usersCollection).doc(user.uid);
        final doc = await transaction.get(docRef);

        if (!doc.exists) {
          throw Exception('Documento de usuario no encontrado');
        }

        final data = doc.data()!;
        final progress = data[progressField] as Map<String, dynamic>? ?? {};
        
        final currentProgress = progress['currentDailyProgress'] as int? ?? 0;
        final totalMinutes = progress['totalMinutes'] as int? ?? 0;
        final totalLessons = progress['totalLessons'] as int? ?? 0;
        final currentExp = progress['experience'] as int? ?? 0;
        final currentCoins = progress['coins'] as int? ?? 0;
        //final currentLevel = progress['level'] as int? ?? 1;

        // Calcular nuevo nivel (cada 100 exp sube de nivel)
        final newExp = currentExp + experienceGained;
        final newLevel = 1 + (newExp ~/ 100);

        transaction.update(docRef, {
          '$progressField.currentDailyProgress': currentProgress + minutesCompleted,
          '$progressField.totalMinutes': totalMinutes + minutesCompleted,
          '$progressField.totalLessons': totalLessons + 1,
          '$progressField.experience': newExp,
          '$progressField.coins': currentCoins + coinsEarned,
          '$progressField.level': newLevel,
          '$progressField.lastActivityDate': FieldValue.serverTimestamp(),
        });

        // Agregar a historial de lecciones
        final lessonHistory = {
          'lessonId': lessonId,
          'completedAt': FieldValue.serverTimestamp(),
          'minutesSpent': minutesCompleted,
          'experienceGained': experienceGained,
          'coinsEarned': coinsEarned,
        };

        transaction.update(docRef, {
          'lessonHistory': FieldValue.arrayUnion([lessonHistory]),
        });
      });

      print('✅ Progreso actualizado: +$minutesCompleted min, +$experienceGained exp');
    } catch (e) {
      print('❌ Error actualizando progreso: $e');
      rethrow;
    }
  }

  // Reiniciar progreso diario
  Future<void> resetDailyProgress() async {
    try {
      final user = _auth.currentUser;
      if (user == null) throw Exception('Usuario no autenticado');

      await _firestore
          .collection(usersCollection)
          .doc(user.uid)
          .update({
        '$progressField.currentDailyProgress': 0,
        '$progressField.lastUpdated': FieldValue.serverTimestamp(),
      });
      print('✅ Progreso diario reiniciado');
    } catch (e) {
      print('❌ Error reiniciando progreso: $e');
      rethrow;
    }
  }

  // Actualizar racha (streak)
  Future<void> updateStreak() async {
    try {
      final user = _auth.currentUser;
      if (user == null) throw Exception('Usuario no autenticado');

      await _firestore.runTransaction((transaction) async {
        final docRef = _firestore.collection(usersCollection).doc(user.uid);
        final doc = await transaction.get(docRef);

        if (!doc.exists) return;

        final data = doc.data()!;
        final progress = data[progressField] as Map<String, dynamic>? ?? {};
        
        final lastActivity = progress['lastActivityDate'] as Timestamp?;
        final currentStreak = progress['currentStreak'] as int? ?? 0;
        final longestStreak = progress['longestStreak'] as int? ?? 0;

        final now = DateTime.now();
        final yesterday = now.subtract(const Duration(days: 1));

        int newStreak = currentStreak;
        
        if (lastActivity != null) {
          final lastActivityDate = lastActivity.toDate();
          
          // Si la última actividad fue ayer, incrementar racha
          if (lastActivityDate.year == yesterday.year &&
              lastActivityDate.month == yesterday.month &&
              lastActivityDate.day == yesterday.day) {
            newStreak = currentStreak + 1;
          }
          // Si la última actividad fue hoy, mantener racha
          else if (lastActivityDate.year == now.year &&
                   lastActivityDate.month == now.month &&
                   lastActivityDate.day == now.day) {
            newStreak = currentStreak;
          }
          // Si pasó más de un día, reiniciar racha
          else {
            newStreak = 1;
          }
        } else {
          newStreak = 1;
        }

        final newLongestStreak = newStreak > longestStreak ? newStreak : longestStreak;

        transaction.update(docRef, {
          '$progressField.currentStreak': newStreak,
          '$progressField.longestStreak': newLongestStreak,
          '$progressField.lastActivityDate': FieldValue.serverTimestamp(),
        });
      });

      print('✅ Racha actualizada');
    } catch (e) {
      print('❌ Error actualizando racha: $e');
      rethrow;
    }
  }

  // ===== STREAMS PARA TIEMPO REAL =====

  // Stream de datos del usuario
  Stream<DocumentSnapshot> getUserDataStream([String? userId]) {
    try {
      final uid = userId ?? _auth.currentUser?.uid;
      if (uid == null) throw Exception('Usuario no autenticado');

      return _firestore
          .collection(usersCollection)
          .doc(uid)
          .snapshots();
    } catch (e) {
      print('❌ Error creando stream de usuario: $e');
      rethrow;
    }
  }

  // Stream específico del progreso
  Stream<Map<String, dynamic>?> getProgressStream() {
    return getUserDataStream().map((snapshot) {
      if (snapshot.exists) {
        final data = snapshot.data() as Map<String, dynamic>;
        return data[progressField] as Map<String, dynamic>?;
      }
      return null;
    });
  }

  // ===== MÉTODOS DE CONSULTA =====

  // Obtener ranking de usuarios por experiencia
  Future<List<Map<String, dynamic>>> getLeaderboard({int limit = 50}) async {
    try {
      final query = await _firestore
          .collection(usersCollection)
          .orderBy('$progressField.experience', descending: true)
          .limit(limit)
          .get();

      return query.docs.map((doc) {
        final data = doc.data();
        return {
          'uid': doc.id,
          'userInfo': data[userInfoField],
          'progress': data[progressField],
        };
      }).toList();
    } catch (e) {
      print('❌ Error obteniendo leaderboard: $e');
      rethrow;
    }
  }

  // Verificar si el usuario existe
  Future<bool> userExists(String uid) async {
    try {
      final doc = await _firestore.collection(usersCollection).doc(uid).get();
      return doc.exists;
    } catch (e) {
      print('❌ Error verificando existencia de usuario: $e');
      return false;
    }
  }
}
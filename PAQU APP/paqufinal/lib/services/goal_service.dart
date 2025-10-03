import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/goal_model.dart';

class GoalService {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  // Crear nueva meta
  Future<void> createUserGoal(UserGoal goal) async {
    try {
      await _firestore
          .collection('user_goals')
          .doc(goal.goalId)
          .set(goal.toMap());
    } catch (e) {
      throw Exception('Error creating goal: $e');
    }
  }

  // Obtener meta activa del usuario
  Stream<UserGoal?> getActiveUserGoal(String userId) {
    return _firestore
        .collection('user_goals')
        .where('userId', isEqualTo: userId)
        .where('isActive', isEqualTo: true)
        .snapshots()
        .map((snapshot) {
      if (snapshot.docs.isEmpty) return null;
      return UserGoal.fromMap(snapshot.docs.first.data());
    });
  }

  // Actualizar progreso diario
  Future<void> updateDailyProgress(String goalId, int minutesCompleted) async {
    try {
      await _firestore.collection('user_goals').doc(goalId).update({
        'completedMinutes': minutesCompleted,
        'currentDay': FieldValue.increment(1),
      });
    } catch (e) {
      throw Exception('Error updating progress: $e');
    }
  }

  // Completar meta y crear nueva
  Future<void> completeGoalAndCreateNew(
      String oldGoalId, UserGoal newGoal) async {
    try {
      final batch = _firestore.batch();
      
      // Desactivar meta anterior
      batch.update(
        _firestore.collection('user_goals').doc(oldGoalId),
        {'isActive': false},
      );
      
      // Crear nueva meta
      batch.set(
        _firestore.collection('user_goals').doc(newGoal.goalId),
        newGoal.toMap(),
      );
      
      await batch.commit();
    } catch (e) {
      throw Exception('Error completing goal: $e');
    }
  }
}
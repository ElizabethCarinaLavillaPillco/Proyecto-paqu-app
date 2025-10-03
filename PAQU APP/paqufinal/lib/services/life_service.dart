import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/life_model.dart';

class LifeService {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  // Inicializar vidas para usuario nuevo
  Future<void> initializeUserLives(String userId) async {
    try {
      final initialLives = UserLives(
        userId: userId,
        currentLives: 5,
        maxLives: 5,
        lastLifeUpdate: DateTime.now(),
        nextFullRecharge: null,
        diamonds: 10, // Diamantes iniciales
      );

      await _firestore
          .collection('user_lives')
          .doc(userId)
          .set(initialLives.toMap());
    } catch (e) {
      throw Exception('Error initializing lives: $e');
    }
  }

  // Obtener vidas del usuario
  Stream<UserLives> getUserLives(String userId) {
    return _firestore
        .collection('user_lives')
        .doc(userId)
        .snapshots()
        .map((snapshot) {
      if (!snapshot.exists) {
        return UserLives(
          userId: userId,
          currentLives: 5,
          maxLives: 5,
          lastLifeUpdate: DateTime.now(),
          diamonds: 0,
        );
      }
      
      final lives = UserLives.fromMap(snapshot.data()!);
      return lives.rechargeLives(); // Recargar automáticamente
    });
  }

  // Usar una vida
  Future<bool> useLife(String userId) async {
    try {
      final doc = await _firestore.collection('user_lives').doc(userId).get();
      
      if (!doc.exists) {
        await initializeUserLives(userId);
        return true;
      }

      var lives = UserLives.fromMap(doc.data()!);
      lives = lives.rechargeLives(); // Recargar antes de verificar

      if (lives.currentLives <= 0) {
        return false; // No hay vidas disponibles
      }

      final updatedLives = UserLives(
        userId: userId,
        currentLives: lives.currentLives - 1,
        maxLives: lives.maxLives,
        lastLifeUpdate: DateTime.now(),
        nextFullRecharge: lives.currentLives - 1 == 0 
            ? DateTime.now().add(Duration(hours: 24))
            : lives.nextFullRecharge,
        diamonds: lives.diamonds,
      );

      await _firestore
          .collection('user_lives')
          .doc(userId)
          .update(updatedLives.toMap());

      return true;
    } catch (e) {
      throw Exception('Error using life: $e');
    }
  }

  // Recargar vidas con diamantes
  Future<void> rechargeWithDiamonds(String userId, int diamondsCost) async {
    try {
      final doc = await _firestore.collection('user_lives').doc(userId).get();
      final lives = UserLives.fromMap(doc.data()!);

      if (lives.diamonds < diamondsCost) {
        throw Exception('Diamantes insuficientes');
      }

      final updatedLives = UserLives(
        userId: userId,
        currentLives: lives.maxLives,
        maxLives: lives.maxLives,
        lastLifeUpdate: DateTime.now(),
        nextFullRecharge: null,
        diamonds: lives.diamonds - diamondsCost,
      );

      await _firestore
          .collection('user_lives')
          .doc(userId)
          .update(updatedLives.toMap());
    } catch (e) {
      throw Exception('Error recharging with diamonds: $e');
    }
  }

  // Agregar diamantes (para recompensas de anuncios)
  Future<void> addDiamonds(String userId, int diamonds) async {
    try {
      await _firestore
          .collection('user_lives')
          .doc(userId)
          .update({
            'diamonds': FieldValue.increment(diamonds),
          });
    } catch (e) {
      throw Exception('Error adding diamonds: $e');
    }
  }
}
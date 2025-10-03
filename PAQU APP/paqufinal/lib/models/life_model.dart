class UserLives {
  final String userId;
  int currentLives;
  int maxLives;
  DateTime lastLifeUpdate;
  DateTime? nextFullRecharge;
  int diamonds;

  UserLives({
    required this.userId,
    required this.currentLives,
    this.maxLives = 5,
    required this.lastLifeUpdate,
    this.nextFullRecharge,
    this.diamonds = 0,
  });

  Map<String, dynamic> toMap() {
    return {
      'userId': userId,
      'currentLives': currentLives,
      'maxLives': maxLives,
      'lastLifeUpdate': lastLifeUpdate.millisecondsSinceEpoch,
      'nextFullRecharge': nextFullRecharge?.millisecondsSinceEpoch,
      'diamonds': diamonds,
    };
  }

  static UserLives fromMap(Map<String, dynamic> map) {
    return UserLives(
      userId: map['userId'],
      currentLives: map['currentLives'],
      maxLives: map['maxLives'],
      lastLifeUpdate: DateTime.fromMillisecondsSinceEpoch(map['lastLifeUpdate']),
      nextFullRecharge: map['nextFullRecharge'] != null 
          ? DateTime.fromMillisecondsSinceEpoch(map['nextFullRecharge'])
          : null,
      diamonds: map['diamonds'] ?? 0,
    );
  }

  // Calcular tiempo restante para recarga completa
  Duration get timeUntilFullRecharge {
    if (nextFullRecharge == null) return Duration.zero;
    return nextFullRecharge!.difference(DateTime.now());
  }

  // Calcular tiempo para recargar 1 vida
  Duration get timePerLife {
    return Duration(hours: 24) ~/ maxLives;
  }

  // Calcular tiempo para recargar vidas faltantes
  Duration get timeUntilNextLife {
    if (currentLives >= maxLives) return Duration.zero;
    
    final timeSinceLastUpdate = DateTime.now().difference(lastLifeUpdate);
    final timePerLife = Duration(hours: 24) ~/ maxLives;
    
    if (timeSinceLastUpdate >= timePerLife) {
      return Duration.zero;
    } else {
      return timePerLife - timeSinceLastUpdate;
    }
  }

  // Verificar si puede recargar una vida
  bool get canRechargeLife {
    return DateTime.now().difference(lastLifeUpdate) >= timePerLife;
  }

  // Recargar vidas basado en el tiempo
  UserLives rechargeLives() {
    final now = DateTime.now();
    final timeSinceLastUpdate = now.difference(lastLifeUpdate);
    final timePerLife = Duration(hours: 24) ~/ maxLives;
    
    int livesToAdd = (timeSinceLastUpdate.inSeconds / timePerLife.inSeconds).floor();
    livesToAdd = livesToAdd.clamp(0, maxLives - currentLives);
    
    if (livesToAdd > 0) {
      return UserLives(
        userId: userId,
        currentLives: currentLives + livesToAdd,
        maxLives: maxLives,
        lastLifeUpdate: now,
        nextFullRecharge: nextFullRecharge,
        diamonds: diamonds,
      );
    }
    
    return this;
  }
}
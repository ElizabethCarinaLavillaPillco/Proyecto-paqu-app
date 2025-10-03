class UserGoal {
  final String userId;
  final String goalId;
  final int dailyMinutes;
  final int totalDays;
  final DateTime startDate;
  final DateTime endDate;
  final int currentDay;
  final int completedMinutes;
  final String difficultyLevel;
  final bool isActive;

  UserGoal({
    required this.userId,
    required this.goalId,
    required this.dailyMinutes,
    required this.totalDays,
    required this.startDate,
    required this.endDate,
    required this.currentDay,
    required this.completedMinutes,
    required this.difficultyLevel,
    required this.isActive,
  });

  Map<String, dynamic> toMap() {
    return {
      'userId': userId,
      'goalId': goalId,
      'dailyMinutes': dailyMinutes,
      'totalDays': totalDays,
      'startDate': startDate.millisecondsSinceEpoch,
      'endDate': endDate.millisecondsSinceEpoch,
      'currentDay': currentDay,
      'completedMinutes': completedMinutes,
      'difficultyLevel': difficultyLevel,
      'isActive': isActive,
      'createdAt': DateTime.now().millisecondsSinceEpoch,
    };
  }

  static UserGoal fromMap(Map<String, dynamic> map) {
    return UserGoal(
      userId: map['userId'],
      goalId: map['goalId'],
      dailyMinutes: map['dailyMinutes'],
      totalDays: map['totalDays'],
      startDate: DateTime.fromMillisecondsSinceEpoch(map['startDate']),
      endDate: DateTime.fromMillisecondsSinceEpoch(map['endDate']),
      currentDay: map['currentDay'],
      completedMinutes: map['completedMinutes'],
      difficultyLevel: map['difficultyLevel'],
      isActive: map['isActive'],
    );
  }

  UserGoal copyWith({
    int? currentDay,
    int? completedMinutes,
    bool? isActive,
  }) {
    return UserGoal(
      userId: userId,
      goalId: goalId,
      dailyMinutes: dailyMinutes,
      totalDays: totalDays,
      startDate: startDate,
      endDate: endDate,
      currentDay: currentDay ?? this.currentDay,
      completedMinutes: completedMinutes ?? this.completedMinutes,
      difficultyLevel: difficultyLevel,
      isActive: isActive ?? this.isActive,
    );
  }
}
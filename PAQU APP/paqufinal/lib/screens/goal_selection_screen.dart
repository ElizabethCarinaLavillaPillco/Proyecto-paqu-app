import 'package:flutter/material.dart';
import 'package:paqufinal/services/goal_service.dart';
import 'package:paqufinal/models/goal_model.dart';
import 'package:paqufinal/widgets/animated_button.dart';
import 'package:paqufinal/widgets/glow_card.dart';

class GoalSelectionScreen extends StatefulWidget {
  final bool isFirstTime;

  const GoalSelectionScreen({super.key, this.isFirstTime = true});

  @override
  State<GoalSelectionScreen> createState() => _GoalSelectionScreenState();
}

class _GoalSelectionScreenState extends State<GoalSelectionScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnimation;
  late Animation<double> _scaleAnimation;

  int? _selectedMinutes;
  int? _selectedDays;
  final GoalService _goalService = GoalService();

  final Map<String, int> _difficultyLevels = {
    'Casual 🎯': 3,
    'Regular ⚡': 7,
    'Serio 🔥': 15,
    'Insane 💀': 20,
  };

  final List<int> _dayOptions = [7, 14, 21];

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1000),
    );

    _fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(
        parent: _controller,
        curve: const Interval(0.0, 0.6, curve: Curves.easeOut),
      ),
    );

    _scaleAnimation = Tween<double>(begin: 0.8, end: 1.0).animate(
      CurvedAnimation(
        parent: _controller,
        curve: const Interval(0.4, 1.0, curve: Curves.elasticOut),
      ),
    );

    _controller.forward();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _selectMinutes(int minutes) {
    setState(() {
      _selectedMinutes = minutes;
    });
  }

  void _selectDays(int days) {
    setState(() {
      _selectedDays = days;
    });
  }

  Future<void> _saveGoal() async {
    if (_selectedMinutes == null || _selectedDays == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Por favor selecciona minutos y días',
            style: TextStyle(
              color: Colors.white,
              fontWeight: FontWeight.bold,
            ),
          ),
          backgroundColor: Colors.orange,
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(15),
          ),
        ),
      );
      return;
    }

    try {
      // TEMPORAL: Usar un userId fijo hasta que implementemos autenticación
      final String userId = 'user_temp_${DateTime.now().millisecondsSinceEpoch}';
      
      final String difficulty = _difficultyLevels.entries
          .firstWhere((entry) => entry.value == _selectedMinutes)
          .key;

      final newGoal = UserGoal(
        userId: userId,
        goalId: '${userId}_${DateTime.now().millisecondsSinceEpoch}',
        dailyMinutes: _selectedMinutes!,
        totalDays: _selectedDays!,
        startDate: DateTime.now(),
        endDate: DateTime.now().add(Duration(days: _selectedDays!)),
        currentDay: 1,
        completedMinutes: 0,
        difficultyLevel: difficulty,
        isActive: true,
      );

      await _goalService.createUserGoal(newGoal);

      // Navegar a la pantalla principal con animación
      Navigator.pushNamedAndRemoveUntil(
        context,
        '/home',
        (route) => false,
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Error al guardar la meta: $e',
            style: TextStyle(
              color: Colors.white,
              fontWeight: FontWeight.bold,
            ),
          ),
          backgroundColor: Colors.red,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[900],
      body: SafeArea(
        child: FadeTransition(
          opacity: _fadeAnimation,
          child: ScaleTransition(
            scale: _scaleAnimation,
            child: Padding(
              padding: const EdgeInsets.all(24.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Header con animación
                  _buildHeader(),
                  const SizedBox(height: 40),

                  // Selección de minutos diarios
                  _buildMinutesSelection(),
                  const SizedBox(height: 40),

                  // Selección de días
                  _buildDaysSelection(),
                  const SizedBox(height: 40),

                  // Botón de confirmación
                  _buildConfirmButton(),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        AnimatedOpacity(
          opacity: _fadeAnimation.value,
          duration: const Duration(milliseconds: 800),
          child: Text(
            widget.isFirstTime 
                ? '¡Configura tu Meta! 🚀'
                : '¡Nueva Meta! 🔄',
            style: const TextStyle(
              fontSize: 32,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
        ),
        const SizedBox(height: 10),
        AnimatedOpacity(
          opacity: _fadeAnimation.value,
          duration: const Duration(milliseconds: 1000),
          child: Text(
            widget.isFirstTime
                ? 'Elige tu desafío diario y la duración'
                : 'Actualiza tu progreso con nuevos objetivos',
            style: TextStyle(
              fontSize: 16,
              color: Colors.grey[400],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildMinutesSelection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Minutos Diarios 🕒',
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        const SizedBox(height: 16),
        Wrap(
          spacing: 12,
          runSpacing: 12,
          children: _difficultyLevels.entries.map((entry) {
            final isSelected = _selectedMinutes == entry.value;
            return AnimatedButton(
              onTap: () => _selectMinutes(entry.value),
              child: GlowCard(
                isSelected: isSelected,
                child: Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 20,
                    vertical: 15,
                  ),
                  child: Column(
                    children: [
                      Text(
                        entry.key,
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: isSelected ? Colors.white : Colors.grey[400],
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '${entry.value} min',
                        style: TextStyle(
                          fontSize: 14,
                          color: isSelected ? Colors.amber : Colors.grey[500],
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildDaysSelection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Duración del Reto 📅',
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        const SizedBox(height: 16),
        Wrap(
          spacing: 12,
          runSpacing: 12,
          children: _dayOptions.map((days) {
            final isSelected = _selectedDays == days;
            return AnimatedButton(
              onTap: () => _selectDays(days),
              child: GlowCard(
                isSelected: isSelected,
                child: Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 24,
                    vertical: 15,
                  ),
                  child: Column(
                    children: [
                      Text(
                        '$days días',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: isSelected ? Colors.white : Colors.grey[400],
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Hasta ${DateTime.now().add(Duration(days: days)).day}/${DateTime.now().add(Duration(days: days)).month}',
                        style: TextStyle(
                          fontSize: 12,
                          color: isSelected ? Colors.green : Colors.grey[500],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildConfirmButton() {
    final isEnabled = _selectedMinutes != null && _selectedDays != null;
    
    return Center(
      child: AnimatedButton(
        onTap: isEnabled ? _saveGoal : null,
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.symmetric(vertical: 16),
          decoration: BoxDecoration(
            gradient: isEnabled
                ? LinearGradient(
                    colors: [Colors.blue.shade600, Colors.purple.shade600],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  )
                : LinearGradient(
                    colors: [Colors.grey.shade700, Colors.grey.shade800],
                  ),
            borderRadius: BorderRadius.circular(25),
            boxShadow: isEnabled
                ? [
                    BoxShadow(
                      color: Colors.blue.withOpacity(0.5),
                      blurRadius: 15,
                      spreadRadius: 2,
                    )
                  ]
                : [],
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                widget.isFirstTime ? 'Comenzar Reto' : 'Actualizar Meta',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
              ),
              const SizedBox(width: 8),
              AnimatedRotation(
                duration: const Duration(milliseconds: 500),
                turns: isEnabled ? 0.25 : 0,
                child: const Icon(
                  Icons.arrow_forward,
                  color: Colors.white,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
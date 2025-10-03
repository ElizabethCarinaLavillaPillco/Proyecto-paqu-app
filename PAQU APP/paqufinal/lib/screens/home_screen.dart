import 'package:flutter/material.dart';
import 'package:paqufinal/models/life_model.dart';
import 'package:paqufinal/services/life_service.dart';
import 'package:paqufinal/widgets/lives_header.dart';
import 'package:paqufinal/widgets/no_lives_dialog.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with SingleTickerProviderStateMixin {
  late AnimationController _animationController;
  late Animation<double> _fadeAnimation;
  late Animation<double> _slideAnimation;

  final LifeService _lifeService = LifeService();
  final String _currentUserId = 'user_temp_123'; // Temporal hasta auth

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 800),
    );

    _fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(
        parent: _animationController,
        curve: const Interval(0.0, 0.5, curve: Curves.easeOut),
      ),
    );

    _slideAnimation = Tween<double>(begin: 50.0, end: 0.0).animate(
      CurvedAnimation(
        parent: _animationController,
        curve: const Interval(0.3, 1.0, curve: Curves.easeOut),
      ),
    );

    _animationController.forward();

    // Inicializar vidas si es primera vez
    _initializeLives();
  }

  Future<void> _initializeLives() async {
    try {
      await _lifeService.initializeUserLives(_currentUserId);
    } catch (e) {
      print('Error initializing lives: $e');
    }
  }

  void _showNoLivesDialog(UserLives lives) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => NoLivesDialog(
        timeUntilRecharge: lives.timeUntilFullRecharge,
        diamonds: lives.diamonds,
        onWatchAd: () {
          Navigator.pop(context);
          _watchAdForLife();
        },
        onUseDiamonds: () {
          Navigator.pop(context);
          _useDiamondsForLives();
        },
        onWait: () {
          Navigator.pop(context);
        },
      ),
    );
  }

  Future<void> _watchAdForLife() async {
    // Simular anuncio
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Anuncio visto - +1 vida'),
        backgroundColor: Colors.green,
      ),
    );
    
    // Aquí integrarías tu SDK de anuncios
    await _lifeService.addDiamonds(_currentUserId, 1); // Recompensa temporal
  }

  Future<void> _useDiamondsForLives() async {
    try {
      await _lifeService.rechargeWithDiamonds(_currentUserId, 10);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('¡5 vidas recargadas!'),
          backgroundColor: Colors.purple,
        ),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  Future<void> _simulateLesson() async {
    final success = await _lifeService.useLife(_currentUserId);
    
    if (!success) {
      // Mostrar diálogo de sin vidas
      final livesDoc = await _lifeService.getUserLives(_currentUserId).first;
      _showNoLivesDialog(livesDoc);
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('¡Lección comenzada! Vidas restantes: ${(await _lifeService.getUserLives(_currentUserId).first).currentLives - 1}'),
          backgroundColor: Colors.blue,
        ),
      );
    }
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[900],
      body: SafeArea(
        child: StreamBuilder<UserLives>(
          stream: _lifeService.getUserLives(_currentUserId),
          builder: (context, snapshot) {
            final lives = snapshot.data ?? UserLives(
              userId: _currentUserId,
              currentLives: 5,
              maxLives: 5,
              lastLifeUpdate: DateTime.now(),
              diamonds: 0,
            );

            return Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                children: [
                  // Header con vidas
                  LivesHeader(
                    lives: lives,
                    onLivesTap: () {
                      if (lives.currentLives < lives.maxLives) {
                        _showNoLivesDialog(lives);
                      }
                    },
                    onDiamondsTap: () {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text('Tienes ${lives.diamonds} diamantes'),
                        ),
                      );
                    },
                  ),
                  const SizedBox(height: 30),

                  // Contenido principal
                  Expanded(
                    child: _buildMainContent(lives),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  Widget _buildMainContent(UserLives lives) {
    return AnimatedBuilder(
      animation: _animationController,
      builder: (context, child) {
        return Transform.translate(
          offset: Offset(0, _slideAnimation.value),
          child: Opacity(
            opacity: _fadeAnimation.value,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '¡Bienvenido! 👋',
                  style: TextStyle(
                    fontSize: 32,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  'Elige una lección para comenzar',
                  style: TextStyle(
                    fontSize: 16,
                    color: Colors.grey[400],
                  ),
                ),
                const SizedBox(height: 40),

                // Botón de lección de ejemplo
                GestureDetector(
                  onTap: _simulateLesson,
                  child: Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(24),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        colors: [Colors.blue.shade800, Colors.purple.shade800],
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                      ),
                      borderRadius: BorderRadius.circular(25),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.blue.withOpacity(0.3),
                          blurRadius: 20,
                          spreadRadius: 2,
                        ),
                      ],
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Icon(Icons.school, color: Colors.white, size: 30),
                            const SizedBox(width: 15),
                            Text(
                              'Lección 1: Saludos',
                              style: TextStyle(
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                                color: Colors.white,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 15),
                        Text(
                          'Aprende los saludos básicos en Quechua',
                          style: TextStyle(
                            color: Colors.white.withOpacity(0.8),
                          ),
                        ),
                        const SizedBox(height: 10),
                        Row(
                          children: [
                            Icon(Icons.heart_broken, color: Colors.white, size: 16),
                            const SizedBox(width: 5),
                            Text(
                              'Usa 1 vida',
                              style: TextStyle(
                                color: Colors.white.withOpacity(0.8),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
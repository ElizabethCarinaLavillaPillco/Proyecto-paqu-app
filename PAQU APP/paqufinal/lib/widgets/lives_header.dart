import 'package:flutter/material.dart';
import '../models/life_model.dart';

class LivesHeader extends StatelessWidget {
  final UserLives lives;
  final VoidCallback? onLivesTap;
  final VoidCallback? onDiamondsTap;

  const LivesHeader({
    super.key,
    required this.lives,
    this.onLivesTap,
    this.onDiamondsTap,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
      decoration: BoxDecoration(
        color: Colors.grey[900],
        border: Border.all(color: Colors.grey[800]!),
        borderRadius: BorderRadius.circular(25),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // Vidas
          GestureDetector(
            onTap: onLivesTap,
            child: _buildLivesWidget(),
          ),

          // Racha de días
          _buildStreakWidget(),

          // Diamantes
          GestureDetector(
            onTap: onDiamondsTap,
            child: _buildDiamondsWidget(),
          ),

          // Idioma
          _buildLanguageWidget(),
        ],
      ),
    );
  }

  Widget _buildLivesWidget() {
    final hasFullLives = lives.currentLives >= lives.maxLives;
    final timeUntilNext = lives.timeUntilNextLife;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.grey[800],
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        children: [
          // Icono de vida
          Icon(
            hasFullLives ? Icons.favorite : Icons.favorite_border,
            color: hasFullLives ? Colors.red : Colors.grey,
            size: 20,
          ),
          const SizedBox(width: 6),
          
          // Contador de vidas
          Text(
            '${lives.currentLives}/5',
            style: TextStyle(
              color: Colors.white,
              fontWeight: FontWeight.bold,
              fontSize: 14,
            ),
          ),

          // Timer si no tiene vidas completas
          if (!hasFullLives && timeUntilNext.inSeconds > 0) ...[
            const SizedBox(width: 6),
            Icon(
              Icons.access_time,
              color: Colors.orange,
              size: 16,
            ),
            const SizedBox(width: 4),
            Text(
              _formatDuration(timeUntilNext),
              style: TextStyle(
                color: Colors.orange,
                fontSize: 12,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildStreakWidget() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.orange.withOpacity(0.2),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: Colors.orange),
      ),
      child: Row(
        children: [
          Icon(
            Icons.local_fire_department,
            color: Colors.orange,
            size: 18,
          ),
          const SizedBox(width: 6),
          Text(
            '7 días', // Esto vendrá de Firebase después
            style: TextStyle(
              color: Colors.orange,
              fontWeight: FontWeight.bold,
              fontSize: 14,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDiamondsWidget() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [Colors.blue.shade600, Colors.purple.shade600],
        ),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        children: [
          Icon(
            Icons.diamond,
            color: Colors.white,
            size: 18,
          ),
          const SizedBox(width: 6),
          Text(
            '${lives.diamonds}',
            style: TextStyle(
              color: Colors.white,
              fontWeight: FontWeight.bold,
              fontSize: 14,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLanguageWidget() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.green.withOpacity(0.2),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: Colors.green),
      ),
      child: Row(
        children: [
          Text(
            'QU',
            style: TextStyle(
              color: Colors.green,
              fontWeight: FontWeight.bold,
              fontSize: 14,
            ),
          ),
        ],
      ),
    );
  }

  String _formatDuration(Duration duration) {
    if (duration.inHours > 0) {
      return '${duration.inHours}h';
    } else if (duration.inMinutes > 0) {
      return '${duration.inMinutes}m';
    } else {
      return '${duration.inSeconds}s';
    }
  }
}
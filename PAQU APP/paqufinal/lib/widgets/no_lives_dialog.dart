import 'package:flutter/material.dart';

class NoLivesDialog extends StatefulWidget {
  final Duration timeUntilRecharge;
  final int diamonds;
  final VoidCallback onWatchAd;
  final VoidCallback onUseDiamonds;
  final VoidCallback onWait;

  const NoLivesDialog({
    super.key,
    required this.timeUntilRecharge,
    required this.diamonds,
    required this.onWatchAd,
    required this.onUseDiamonds,
    required this.onWait,
  });

  @override
  State<NoLivesDialog> createState() => _NoLivesDialogState();
}

class _NoLivesDialogState extends State<NoLivesDialog> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _scaleAnimation;
  late Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 600),
    );

    _scaleAnimation = Tween<double>(begin: 0.7, end: 1.0).animate(
      CurvedAnimation(
        parent: _controller,
        curve: Curves.elasticOut,
      ),
    );

    _fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(
        parent: _controller,
        curve: Curves.easeOut,
      ),
    );

    _controller.forward();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      backgroundColor: Colors.transparent,
      child: ScaleTransition(
        scale: _scaleAnimation,
        child: FadeTransition(
          opacity: _fadeAnimation,
          child: Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [Colors.red.shade900, Colors.purple.shade900],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(30),
              boxShadow: [
                BoxShadow(
                  color: Colors.red.withOpacity(0.5),
                  blurRadius: 30,
                  spreadRadius: 5,
                ),
              ],
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Icono de advertencia
                Icon(
                  Icons.heart_broken,
                  color: Colors.white,
                  size: 60,
                ),
                const SizedBox(height: 20),

                // Título
                Text(
                  '¡Te quedaste sin vidas! 💔',
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 10),

                // Descripción
                Text(
                  'Necesitas vidas para continuar aprendiendo',
                  style: TextStyle(
                    fontSize: 16,
                    color: Colors.white.withOpacity(0.8),
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 20),

                // Tiempo de recarga
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Colors.black.withOpacity(0.3),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.access_time,
                        color: Colors.orange,
                      ),
                      const SizedBox(width: 10),
                      Text(
                        'Recarga completa en: ${_formatDuration(widget.timeUntilRecharge)}',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: Colors.orange,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 25),

                // Opción 1: Ver anuncio
                _buildOptionButton(
                  icon: Icons.play_arrow,
                  title: 'Ver Anuncio',
                  subtitle: 'Gana 1 vida gratis',
                  color: Colors.blue,
                  onTap: widget.onWatchAd,
                ),
                const SizedBox(height: 12),

                // Opción 2: Usar diamantes
                _buildOptionButton(
                  icon: Icons.diamond,
                  title: 'Canjear Diamantes',
                  subtitle: '${widget.diamonds >= 10 ? '10 diamantes por 5 vidas' : 'Diamantes insuficientes'}',
                  color: widget.diamonds >= 10 ? Colors.purple : Colors.grey,
                  onTap: widget.diamonds >= 10 ? widget.onUseDiamonds : null,
                ),
                const SizedBox(height: 12),

                // Opción 3: Esperar
                _buildOptionButton(
                  icon: Icons.schedule,
                  title: 'Esperar',
                  subtitle: 'Las vidas se recargan automáticamente',
                  color: Colors.green,
                  onTap: widget.onWait,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildOptionButton({
    required IconData icon,
    required String title,
    required String subtitle,
    required Color color,
    required VoidCallback? onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: color.withOpacity(0.2),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: color),
        ),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: color,
                shape: BoxShape.circle,
              ),
              child: Icon(icon, color: Colors.white, size: 20),
            ),
            const SizedBox(width: 15),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                  Text(
                    subtitle,
                    style: TextStyle(
                      fontSize: 12,
                      color: Colors.white.withOpacity(0.7),
                    ),
                  ),
                ],
              ),
            ),
            Icon(
              Icons.arrow_forward_ios,
              color: Colors.white.withOpacity(0.5),
              size: 16,
            ),
          ],
        ),
      ),
    );
  }

  String _formatDuration(Duration duration) {
    final hours = duration.inHours;
    final minutes = duration.inMinutes.remainder(60);
    
    if (hours > 0) {
      return '${hours}h ${minutes}m';
    } else {
      return '${minutes}m';
    }
  }
}
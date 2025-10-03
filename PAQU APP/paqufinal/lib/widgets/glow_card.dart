import 'package:flutter/material.dart';

class GlowCard extends StatelessWidget {
  final Widget child;
  final bool isSelected;
  final VoidCallback? onTap;

  const GlowCard({
    super.key,
    required this.child,
    this.isSelected = false,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 300),
        decoration: BoxDecoration(
          gradient: isSelected
              ? LinearGradient(
                  colors: [
                    Colors.blue.shade800.withOpacity(0.8),
                    Colors.purple.shade800.withOpacity(0.8),
                  ],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                )
              : LinearGradient(
                  colors: [
                    Colors.grey.shade800.withOpacity(0.6),
                    Colors.grey.shade900.withOpacity(0.6),
                  ],
                ),
          borderRadius: BorderRadius.circular(20),
          border: isSelected
              ? Border.all(color: Colors.blue.shade400, width: 2)
              : Border.all(color: Colors.grey.shade700, width: 1),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color: Colors.blue.withOpacity(0.5),
                    blurRadius: 20,
                    spreadRadius: 3,
                  ),
                  BoxShadow(
                    color: Colors.purple.withOpacity(0.3),
                    blurRadius: 30,
                    spreadRadius: 2,
                  ),
                ]
              : [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.3),
                    blurRadius: 10,
                    spreadRadius: 1,
                  ),
                ],
        ),
        child: child,
      ),
    );
  }
}
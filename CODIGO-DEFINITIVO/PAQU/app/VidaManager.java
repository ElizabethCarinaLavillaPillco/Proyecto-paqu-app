import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

public class VidaManager {

    public static void verificarYRecargarVidas(Activity activity, TextView vidasCount, Runnable onVidasValidas) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("Usuarios")
                    .child(user.getUid());

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long vidas = snapshot.child("vidas").getValue(Long.class);
                    Long ultimaVida0 = snapshot.child("ultimaVida0").getValue(Long.class);
                    long ahora = System.currentTimeMillis();

                    if (vidas == null) vidas = 5;

                    if (vidas <= 0) {
                        if (ultimaVida0 == null) ultimaVida0 = ahora;
                        long tiempoPasado = ahora - ultimaVida0;

                        if (tiempoPasado >= 3600000) {
                            ref.child("vidas").setValue(5);
                            ref.child("ultimaVida0").removeValue();
                            vidasCount.setText("5");
                            Toast.makeText(activity, "🔄 ¡Vidas recargadas!", Toast.LENGTH_SHORT).show();
                            onVidasValidas.run();
                        } else {
                            long minRestantes = (3600000 - tiempoPasado) / 60000;
                            Toast.makeText(activity, "🕐 Espera " + minRestantes + " min", Toast.LENGTH_LONG).show();
                            activity.startActivity(new Intent(activity, ejericicio1.class));
                            activity.finish();
                        }
                    } else {
                        vidasCount.setText(String.valueOf(vidas));
                        onVidasValidas.run();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(activity, "Error al cargar vidas", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

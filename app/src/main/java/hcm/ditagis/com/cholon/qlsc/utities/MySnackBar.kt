package hcm.ditagis.com.cholon.qlsc.utities

import com.google.android.material.snackbar.Snackbar
import android.view.View

/**
 * Created by ThanLe on 26/10/2017.
 */

object MySnackBar {
    fun make(view: View, text: String, isLong: Boolean) {
        val time = if (isLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
        Snackbar.make(view, text, time)
                .setAction("Action", null).show()
    }

    fun make(view: View, id: Int, isLong: Boolean) {
        val time = if (isLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
        Snackbar.make(view, id, time)
                .setAction("Action", null).show()
    }
}

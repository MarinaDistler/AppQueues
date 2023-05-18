package AppQueuesClient.registered

import android.os.Bundle
import android.view.View
import android.widget.TextView
import AppQueuesClient.BaseActivity
import android.opengl.Visibility
import android.widget.EditText
import com.example.app.R
import org.json.JSONObject

class ProfileActivity : BaseActivity() {
    val path = "profile"
    var login: String? = null
    var shop_name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        setContentView(R.layout.activity_profile)
        checkRegistered()
        val answer = network.doHttpGet(path)
        if (network.checkForError(answer, arrayOf("shop_name", "login"), this)) {
            return
        }
        login = answer.getString("login")
        shop_name = answer.getString("shop_name")
        findViewById<TextView>(R.id.textLoginValue).text = login
        findViewById<TextView>(R.id.textShopNameValue).text = shop_name
    }

    fun changePassword(view: View) {
        checkRegistered()
        showDialogTwoEditText("Change password", null,true,
            "Save", {dialog, mview ->
                val edit_text1 = mview.findViewById<EditText>(R.id.dialogEditText1)
                val edit_text2 = mview.findViewById<EditText>(R.id.dialogEditText2)
                val text_error = mview.findViewById<TextView>(R.id.dialogTextError)
                val str1 = edit_text1.text.toString()
                val str2 = edit_text2.text.toString()
                if (str1 != str2) {
                    text_error.visibility = View.VISIBLE
                    text_error.text = "Passwords must be the same!"
                } else if (str1.isEmpty()) {
                    text_error.visibility = View.VISIBLE
                    text_error.text = "The password must not be empty!"
                } else {
                    val answer = network.doHttpPost(path, JSONObject().put("password", str1))
                    network.checkForError(answer, arrayOf(), this)
                    dialog.cancel()
                } },
            "Cancel", {dialog, _ -> dialog.cancel()},
            "Password: ", "",
            "Repeat the password:", ""
        )
    }

    fun editLogin(view: View) {
        showDialogEditText("Edit login", null,true,
            "Save", {dialog, mview ->
                val str = mview.findViewById<EditText>(R.id.dialogEditText).text.toString()
                val answer = network.doHttpPost(path, JSONObject().put("login", str))
                network.checkForError(answer, arrayOf(), this)
                restartActivity()
                dialog.cancel() },
            "Cancel", {dialog, _ -> dialog.cancel()},
            "Login: ", login
        )
    }

    fun editShopName(view: View) {
        showDialogEditText("Edit shop name", null,true,
            "Save", {dialog, mview ->
                val str = mview.findViewById<EditText>(R.id.dialogEditText).text.toString()
                val answer = network.doHttpPost(path, JSONObject().put("shop_name", str))
                network.checkForError(answer, arrayOf(), this)
                restartActivity()
                dialog.cancel() },
            "Cancel", {dialog, _ -> dialog.cancel()},
            "Shop name: ", shop_name
        )
    }
}
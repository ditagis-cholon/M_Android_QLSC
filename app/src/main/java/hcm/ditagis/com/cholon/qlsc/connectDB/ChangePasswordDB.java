package hcm.ditagis.com.cholon.qlsc.connectDB;

import android.content.Context;
import android.os.StrictMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.EncodeMD5;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.User;

public class ChangePasswordDB implements IDB<User, Boolean, String> {
    private Context mContext;

    public ChangePasswordDB(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public Boolean add(User user) {
        return null;
    }

    @Override
    public Boolean delete(String k) {
        return false;
    }

    @Override
    public Boolean update(User user) {
        return null;
    }


    @Override
    public User find(String userName, String oldPassword) {
        Connection cnn = ConnectionDB.getInstance().getConnection();
        User user = null;
        ResultSet rs = null;
        try {
            if (cnn == null)
                return null;
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            String query = mContext.getString(R.string.sql_login);
            PreparedStatement mStatement = cnn.prepareStatement(query);

            String passEncoded1 = (new EncodeMD5()).encode(oldPassword);
            String passEncoded2 = (new EncodeMD5()).encode(passEncoded1 + mContext.getString(R.string.encode_string));
            String passEncoded = (passEncoded1 + ":" + passEncoded2).replace("-", "");

            mStatement.setString(1, userName);
            mStatement.setString(2, passEncoded);
            rs = mStatement.executeQuery();

            while (rs.next()) {

                user = new User();
                user.setUserName(userName);
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (rs != null && !rs.isClosed())
                    rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return user;
    }
    @Override
    public User find(String danhBo, String oldPassword, String newPassword) {
     return null;
    }

    public User change(String userName, String newPassword) {
        Connection cnn = ConnectionDB.getInstance().getConnection();
        User user = null;
        ResultSet rs = null;
        try {
            if (cnn == null)
                return null;
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            String query = mContext.getString(R.string.sql_changepassword);
            PreparedStatement mStatement = cnn.prepareStatement(query);

            String passEncoded1 = (new EncodeMD5()).encode(newPassword);
            String passEncoded2 = (new EncodeMD5()).encode(passEncoded1 + mContext.getString(R.string.encode_string));
            String passEncoded = (passEncoded1 + ":" + passEncoded2).replace("-", "");

            mStatement.setString(1, passEncoded);
            mStatement.setString(2, passEncoded);
            mStatement.setString(3, userName);
            int update = mStatement.executeUpdate();

            if (update > 0) {

                user = new User();
                user.setUserName(userName);
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (rs != null && !rs.isClosed())
                    rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return user;
    }
    @Override
    public List<User> getAll() {
        return null;
    }


}

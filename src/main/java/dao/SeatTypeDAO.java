package dao;

import configs.DBConnection;
import models.SeatType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatTypeDAO {

    public static List<SeatType> getAllSeatTypes() {
        List<SeatType> seatTypes = new ArrayList<>();
        String query = "SELECT * FROM seatTypes";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("seatTypeId");
                String name = rs.getString("seatTypeName");
                double price = rs.getDouble("price");

                SeatType seatType = new SeatType(id, name, price);
                seatTypes.add(seatType);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return seatTypes;
    }



    public static SeatType getSeatTypeById(int seatTypeId) {
        SeatType seatType = null;
        String query = "SELECT * FROM seatTypes WHERE seatTypeId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, seatTypeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                seatType = new SeatType();
                seatType.setSeatTypeId(rs.getInt("seatTypeId"));
                seatType.setSeatTypeName(rs.getString("seatTypeName"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return seatType;
    }


}

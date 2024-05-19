/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package esante;

/**
 *
 * @author ixlam
 */
public interface CalenderEvent {
    
    
    public void OnCellSelected(CalenderCell cell);
    public void OnReservedCellSelected(CalenderCell cell);
    public void OnReserved(CalenderCell cell, Reservation res);
    
}

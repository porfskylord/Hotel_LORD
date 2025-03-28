from flask import Flask, render_template, request, redirect, url_for, flash, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_login import LoginManager, UserMixin, login_user, logout_user, login_required, current_user
import stripe
import os
from datetime import datetime
from sqlalchemy import text


app = Flask(__name__)

app.config['SECRET_KEY'] = os.environ.get('SECRET_KEY', 'your_secret_key')
app.config['SQLALCHEMY_DATABASE_URI'] = 'postgresql://postgres:azad%401234@localhost/HotelLord'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)
login_manager = LoginManager(app)
login_manager.login_view = "login"

stripe.api_key = os.environ.get('STRIPE_SECRET_KEY', 'your_stripe_secret_key')

@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))

class User(db.Model, UserMixin):
    __tablename__ = 'hotel_guests'
    
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(100), nullable=False)
    email = db.Column(db.String(150), unique=True, nullable=False)
    password = db.Column(db.Text, nullable=False)
    contactno = db.Column(db.String(20), unique=True, nullable=False)
    gender = db.Column(db.String(10), nullable=False)   
    aadhar = db.Column(db.String(12), unique=True, nullable=True)
    address = db.Column(db.String(255), nullable=True)
    age = db.Column(db.Integer, nullable=False)

    __table_args__ = (
        db.CheckConstraint('age >= 18', name='check_age_min_18'),
    )

class Room(db.Model):
    __tablename__ = 'room_types'
    id = db.Column(db.Integer, primary_key=True)
    roomtype = db.Column(db.String(255), unique=True, nullable=False)
    ratepernight = db.Column(db.Numeric(10, 2), nullable=False)
    beds = db.Column(db.String(255), unique=True, nullable=False)

class Booking(db.Model):
    __tablename__ = 'hotel_booking_requests'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('hotel_guests.id', ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    roomtype = db.Column(db.Integer, db.ForeignKey('room_types.id', ondelete="RESTRICT", onupdate="CASCADE"), nullable=False)
    checkin_date = db.Column(db.Date, nullable=False)
    checkout_date = db.Column(db.Date, nullable=False)
    total_price = db.Column(db.Numeric(10, 2), nullable=False)
    payment_status = db.Column(db.String(20), default="Pending")
    noofguest = db.Column(db.Integer, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    bookingtype = db.Column(db.String(225))
    isdeleted = db.Column(db.Boolean, nullable=False, server_default="false")

  



@app.route("/")
def home():
    return render_template("index.html")


@app.route("/register", methods=["GET", "POST"])
def register():
    if request.method == "POST":
        username = request.form["username"]
        email = request.form["email"]
        password = bcrypt.generate_password_hash(request.form["password"]).decode("utf-8")
        contactno = request.form["contactNo"]
        gender = request.form["gender"]   
        # aadhar = request.form["aadhar"]
        address = request.form["address"]
        age = int(request.form["age"])

         
        existing_user = User.query.filter(
            (User.email == email) | (User.contactno == contactno) | (User.aadhar == aadhar)
        ).first()
        if existing_user:
            flash("Email, Contact Number, or Aadhar already exists!", "danger")
            return redirect(url_for("home"))   

         
        new_user = User(
            username=username, 
            email=email, 
            password=password, 
            contactno=contactno, 
            gender=gender, 
            aadhar=aadhar, 
            address=address, 
            age=age
        )
        db.session.add(new_user)
        db.session.commit()

        flash("Account Created Successfully! Please Login.", "success")
        return redirect(url_for("home"))   

    return render_template("register.html")



@app.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        email = request.form["email"]
        password = request.form["password"]
        user = User.query.filter_by(email=email).first()

        if not user:
            flash("No account found with this email!", "danger")
            return redirect(url_for("home"))   

        if not bcrypt.check_password_hash(user.password, password):
            flash("Invalid password!", "danger")
            return redirect(url_for("home"))

        login_user(user)
        return redirect(url_for("dashboard"))

    return render_template("login.html")   


@app.route("/logout")
@login_required
def logout():
    logout_user()
    return redirect(url_for("home"))


@app.route("/dashboard")
@login_required
def dashboard():
    query = text("""
        SELECT 
        roomd.roomtype AS "Suite Type", 
        x."No Of Room Available", 
        roomd.ratepernight AS "Price",roomd.beds as Beds
        FROM room_types roomd
        INNER JOIN (
            SELECT 
                rt.id, 
                COUNT(*) - COALESCE(req.reqbooking, 0) AS "No Of Room Available"
            FROM room_types rt 
            INNER JOIN hotel_rooms rm ON rm.roomtypeid = rt.id  
            LEFT JOIN (
                SELECT roomtype, COUNT(*) AS reqbooking 
                FROM hotel_booking_requests 
                GROUP BY roomtype
            ) req ON req.roomtype = rt.id
            WHERE rm.isbooked = FALSE 
            GROUP BY rt.id, req.reqbooking
        ) x ON roomd.id = x.id  where x."No Of Room Available">0 order by roomd.ratepernight; 
    """)

    available_suites = db.session.execute(query).fetchall()
    
    return render_template("dashboard.html", suites=available_suites, current_user=current_user)


@app.route("/my_bookings")
@login_required
def my_bookings():
     
    bookings = db.session.query(
        Booking.id,
        Booking.checkin_date,
        Booking.checkout_date,
        Booking.total_price,
        Booking.payment_status,
        Booking.noofguest,
        Room.roomtype,   
        Room.ratepernight,
        Room.beds
    ).join(Room, Booking.roomtype == Room.id).filter(Booking.user_id == current_user.id).all()

    return render_template("my_bookings.html", bookings=bookings)


@app.route("/book", methods=["POST"])
@login_required
def book_room():
    checkin_date = request.form["checkin_date"]
    checkout_date = request.form["checkout_date"]
    roomtype_name = request.form["room_type"]   
    noofguest = int(request.form["guests"])
    contactno = request.form["contactNo"]
    aadhar = request.form["aadhar"]
    address = request.form["address"]
    gender = request.form["gender"]

    suite = Room.query.filter_by(roomtype=roomtype_name).first()

    if not suite:
        flash("Invalid suite type selected!", "danger")
        return redirect(url_for("dashboard"))

    roomtype_id = suite.id

    num_nights = (datetime.strptime(checkout_date, "%Y-%m-%d") - datetime.strptime(checkin_date, "%Y-%m-%d")).days
    total_price = num_nights * float(suite.ratepernight)

    new_booking = Booking(
        user_id=current_user.id,
        roomtype=roomtype_id,  
        checkin_date=checkin_date,
        checkout_date=checkout_date,
        total_price=total_price,
        noofguest=noofguest,
        bookingtype='Online'
    )

    db.session.add(new_booking)
    db.session.commit()

    flash(f"Booking request submitted for {suite.roomtype} from {checkin_date} to {checkout_date}. Proceed to hotel counter for check-in.", "success")
    return redirect(url_for("dashboard"))


@app.route("/cancel/<int:booking_id>")
@login_required
def cancel_booking(booking_id):
    booking = Booking.query.get(booking_id)

    if not booking:
        flash("Booking not found!", "danger")
        return redirect(url_for("dashboard"))

    # Instead of deleting, update `isdeleted` to True
    booking.isdeleted = True
    db.session.commit()

    flash("Booking request canceled!", "info")
    return redirect(url_for("dashboard"))


# @app.route("/pay/<int:booking_id>", methods=["POST"])
# @login_required
# def pay(booking_id):
#     booking = db.session.get(Booking, booking_id)

#     if not booking:
#         flash("Booking not found!", "danger")
#         return redirect(url_for("dashboard"))

#     try:
#         checkout_session = stripe.checkout.Session.create(
#             payment_method_types=["card"],
#             line_items=[{
#                 "price_data": {
#                     "currency": "usd",
#                     "product_data": {"name": f"Booking ID {booking.id}"},
#                     "unit_amount": int(booking.total_price * 100),
#                 },
#                 "quantity": 1,
#             }],
#             mode="payment",
#             success_url=url_for("payment_success", booking_id=booking.id, _external=True),
#             cancel_url=url_for("dashboard", _external=True),
#         )
#         return jsonify({"checkout_url": checkout_session.url})
#     except stripe.error.AuthenticationError:
#         flash("Payment failed due to invalid Stripe API key.", "danger")
#         return redirect(url_for("dashboard"))


@app.route("/payment_success/<int:booking_id>")
def payment_success(booking_id):
    booking = Booking.query.get(booking_id)

    if booking:
        booking.payment_status = "Paid"
        db.session.commit()

        flash("Payment successful!", "success")

    return redirect(url_for("dashboard"))


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)

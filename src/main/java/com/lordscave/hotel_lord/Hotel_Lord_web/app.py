from flask import Flask, render_template, request, redirect, url_for, flash, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_login import LoginManager, UserMixin, login_user, logout_user, login_required, current_user
import stripe
import os
from datetime import datetime


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
    __tablename__ = 'sec_usersguest'
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(100), unique=True, nullable=False)
    email = db.Column(db.String(150), unique=True, nullable=False)
    password = db.Column(db.Text, nullable=False)

class Room(db.Model):
    __tablename__ = 'comn_roomtypemaster'
    id = db.Column(db.Integer, primary_key=True)
    roomtype = db.Column(db.String(255), unique=True, nullable=False)
    ratepernight = db.Column(db.Numeric(10, 2), nullable=False)

class Booking(db.Model):
    __tablename__ = 'comn_bookings'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('sec_usersguest.id'))
    roomtype = db.Column(db.String(255), db.ForeignKey('comn_roomtypemaster.roomtype'), nullable=False)
    checkin_date = db.Column(db.Date, nullable=False)
    checkout_date = db.Column(db.Date, nullable=False)
    total_price = db.Column(db.Numeric(10, 2), nullable=False)
    payment_status = db.Column(db.String(20), default="Pending")
    noofguest = db.Column(db.Integer, nullable=False)


@app.route("/")
def home():
    return render_template("index.html")


@app.route("/register", methods=["GET", "POST"])
def register():
    if request.method == "POST":
        username = request.form["username"]
        email = request.form["email"]
        password = bcrypt.generate_password_hash(request.form["password"]).decode("utf-8")

        new_user = User(username=username, email=email, password=password)
        db.session.add(new_user)
        db.session.commit()

        flash("Account Created Successfully! Please Login.", "success")
        return redirect(url_for("login"))
    return render_template("register.html")

@app.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        email = request.form["email"]
        password = request.form["password"]
        user = User.query.filter_by(email=email).first()

        if user and bcrypt.check_password_hash(user.password, password):
            login_user(user)
            return redirect(url_for("dashboard"))

        flash("Invalid Credentials!", "danger")
    return render_template("login.html")

@app.route("/logout")
@login_required
def logout():
    logout_user()
    flash("Logged out successfully!", "info")
    return redirect(url_for("home"))


@app.route("/dashboard")
@login_required
def dashboard():
    suites = Room.query.order_by(Room.ratepernight.asc()).all()
    return render_template("dashboard.html", suites=suites, current_user=current_user)

@app.route("/my_bookings")
@login_required
def my_bookings():
    bookings = Booking.query.filter_by(user_id=current_user.id).all()
    return render_template("my_bookings.html", bookings=bookings)

@app.route("/book", methods=["POST"])
@login_required
def book_room():
    checkin_date = request.form["checkin_date"]
    checkout_date = request.form["checkout_date"]
    roomtype = request.form["room_type"]
    noofguest = int(request.form["guests"]) 

    suite = Room.query.filter_by(roomtype=roomtype).first()

    if not suite:
        flash("Invalid suite type selected!", "danger")
        return redirect(url_for("dashboard"))

    num_nights = (datetime.strptime(checkout_date, "%Y-%m-%d") - datetime.strptime(checkin_date, "%Y-%m-%d")).days
    total_price = num_nights * float(suite.ratepernight)

    new_booking = Booking(
        user_id=current_user.id,
        roomtype=roomtype,
        checkin_date=checkin_date,
        checkout_date=checkout_date,
        total_price=total_price,
        noofguest=noofguest
    )

    db.session.add(new_booking)
    db.session.commit()

    flash(f"Booking request submitted for {roomtype} from {checkin_date} to {checkout_date}. Proceed to hotel counter for check-in.", "success")
    return redirect(url_for("dashboard"))

@app.route("/cancel/<int:booking_id>")
@login_required
def cancel_booking(booking_id):
    booking = Booking.query.get(booking_id)

    if not booking:
        flash("Booking not found!", "danger")
        return redirect(url_for("dashboard"))

    db.session.delete(booking)
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
